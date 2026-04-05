package net.hallowed.neatlybetter.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.init.ModTickets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class FastChunkScanner implements MapBuilderItem.MapGenerationTask {

    private static final ForkJoinPool COMPUTE_POOL = new ForkJoinPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            pool -> {
                ForkJoinWorkerThread t =
                        ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 2);
                t.setName("NeatlyBetter-MapWorker-" + t.getPoolIndex());
                return t;
            },
            null, true
    );

    // ──────────────────────────────────────────────────────────────────────
    //  Per-scanner tuning constants
    // ──────────────────────────────────────────────────────────────────────

    /** Map scale threshold for parallel pixel computation. */
    private static final int MIN_PARALLEL_SCALE = 3;

    /** Columns per parallel work segment.  128 / 16 = 8 tasks at scale 3+. */
    private static final int PARALLEL_SEGMENT_SIZE = 16;

    /** Time budget per process() call in nanoseconds (~8 ms). */
    private static final long BUDGET_NANOS = 8_000_000L;

    /** Number of pixel rows to pre-ticket ahead of the current row. */
    private static final int PREFETCH_ROWS_AHEAD = 4;

    /** Heap usage threshold above which we stop adding tickets. */
    private static final double MEMORY_PRESSURE_THRESHOLD = 0.85;

    /** Max tickets active per scanner at any time. */
    private static final int MAX_OUTSTANDING_TICKETS = 48;

    // ──────────────────────────────────────────────────────────────────────
    //  Structure → map-icon mapping
    // ──────────────────────────────────────────────────────────────────────

    private static final Map<Identifier, Holder<@NotNull MapDecorationType>> STRUCTURE_ICONS;

    static {
        STRUCTURE_ICONS = Map.of(
                Identifier.withDefaultNamespace("mansion"),         MapDecorationTypes.WOODLAND_MANSION,
                Identifier.withDefaultNamespace("monument"),        MapDecorationTypes.OCEAN_MONUMENT,
                Identifier.withDefaultNamespace("swamp_hut"),       MapDecorationTypes.SWAMP_HUT,
                Identifier.withDefaultNamespace("jungle_pyramid"),  MapDecorationTypes.JUNGLE_TEMPLE,
                Identifier.withDefaultNamespace("village_plains"),  MapDecorationTypes.PLAINS_VILLAGE,
                Identifier.withDefaultNamespace("village_desert"),  MapDecorationTypes.DESERT_VILLAGE,
                Identifier.withDefaultNamespace("village_savanna"), MapDecorationTypes.SAVANNA_VILLAGE,
                Identifier.withDefaultNamespace("village_snowy"),   MapDecorationTypes.SNOWY_VILLAGE,
                Identifier.withDefaultNamespace("village_taiga"),   MapDecorationTypes.TAIGA_VILLAGE
        );
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Instance fields
    // ══════════════════════════════════════════════════════════════════════

    private final ServerLevel world;
    private final ItemStack  mapStack;
    private final int        centerX;
    private final int        centerZ;
    private final int        i;
    private final int        minChunkX;
    private final int        maxChunkX;
    private final boolean    hasCeiling;

    /** Local chunk cache — keyed by {@link ChunkPos}. */
    private final Map<Long, ChunkAccess> chunkCache = new HashMap<>();

    /** Chunk positions with currently active MAP_SCAN tickets. */
    private final Set<Long> ticketedPositions = new HashSet<>();

    /** Current pixel row being processed (-1 = shadow row, 0–127 = map rows). */
    private int currentRow = -1;

    /**
     * Previous row's averaged heights per column — used for the vanilla height-shadow
     * calculation.  Must be {@code double} (not int) to preserve fractional precision;
     * truncating to int causes incorrect brightness on gentle slopes.
     */
    private final double[] prevColumnHeights = new double[128];

    /** Detected structure positions for decoration. */
    private final Map<String, StructureIconEntry> structureIcons = new LinkedHashMap<>();

    /** Track which structure types we've already detected (avoid duplicates). */
    private final Set<Identifier> detectedStructures = new HashSet<>();

    /** True when the scanner is blocked waiting for chunk generation. */
    private boolean waitingForChunks = false;

    /** Number of tickets currently held by this scanner. */
    private int outstandingTickets = 0;

    // ══════════════════════════════════════════════════════════════════════
    //  Constructor
    // ══════════════════════════════════════════════════════════════════════

    public FastChunkScanner(ServerLevel world, ItemStack mapStack, int centerX, int centerZ, int zoom) {
        this.world    = world;
        this.mapStack = mapStack;
        this.centerX  = centerX;
        this.centerZ  = centerZ;
        this.i        = 1 << zoom;

        // Compute chunk X range covered by this map
        int mapLeft  = centerX / i - 64;
        int mapRight = mapLeft + 127;
        this.minChunkX = (mapLeft  * i) >> 4;
        this.maxChunkX = ((mapRight * i) + (i - 1)) >> 4;

        this.hasCeiling = world.dimensionType().hasCeiling();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MapGenerationTask API
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public boolean process() {
        waitingForChunks = false;

        MapId mapId = mapStack.get(DataComponents.MAP_ID);
        if (mapId == null) { cleanup(); return true; }

        MapItemSavedData state = world.getMapData(mapId);
        if (state == null) { cleanup(); return true; }

        // ── Phase 1: Add tickets for chunks we need ────────────────────
        if (!isMemoryPressured()) {
            submitTickets();
        }

        // ── Phase 2: Poll for chunks that are now ready ────────────────
        pollReadyChunks();

        // ── Phase 3: Draw rows where all chunks are cached ─────────────
        boolean madeProgress = drawReadyRows(state);

        // ── Phase 4: Evict old chunks to free memory ───────────────────
        evictProcessedChunks();

        // ── Done? ──────────────────────────────────────────────────────
        if (currentRow >= 128) {
            applyStructureDecorations(state);
            cleanup();
            return true;
        }

        waitingForChunks = !madeProgress;
        return false;
    }

    @Override
    public boolean isWaiting() {
        return waitingForChunks;
    }

    @Override
    public float getProgress() {
        return Math.max(0, currentRow) / 128f;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Phase 1 — Submit tickets for chunk loading
    // ══════════════════════════════════════════════════════════════════════

    private void submitTickets() {
        ServerChunkCache cs = world.getChunkSource();
        boolean added = false;

        int endRow = Math.min(currentRow + PREFETCH_ROWS_AHEAD, 128);
        for (int r = currentRow; r < endRow; r++) {
            int rowZ  = (centerZ / i + r - 64) * i;
            int minCZ = rowZ >> 4;
            int maxCZ = (rowZ + i - 1) >> 4;

            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    long key = ChunkPos.pack(cx, cz);

                    if (chunkCache.containsKey(key) || ticketedPositions.contains(key)) continue;

                    if (outstandingTickets >= MAX_OUTSTANDING_TICKETS) {
                        if (added) {
                            cs.runDistanceManagerUpdates();
                        }
                        return;
                    }

                    cs.addTicketWithRadius(ModTickets.MAP_SCAN, new ChunkPos(cx, cz), 0);
                    ticketedPositions.add(key);
                    outstandingTickets++;
                    added = true;
                }
            }
        }

        if (added) {
            cs.runDistanceManagerUpdates();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Phase 2 — Poll for ready chunks
    // ══════════════════════════════════════════════════════════════════════

    private void pollReadyChunks() {
        ServerChunkCache cs = world.getChunkSource();

        Iterator<Long> it = ticketedPositions.iterator();
        while (it.hasNext()) {
            long key = it.next();

            // Already cached from a previous poll — just release ticket
            if (chunkCache.containsKey(key)) {
                cs.removeTicketWithRadius(ModTickets.MAP_SCAN,
                        new ChunkPos(ChunkPos.getX(key), ChunkPos.getZ(key)), 0);
                outstandingTickets--;
                it.remove();
                continue;
            }

            // Try to grab the chunk — null means not yet ready
            LevelChunk chunk = cs.getChunkNow(ChunkPos.getX(key), ChunkPos.getZ(key));
            if (chunk != null) {
                chunkCache.put(key, chunk);
                cs.removeTicketWithRadius(ModTickets.MAP_SCAN,
                        new ChunkPos(ChunkPos.getX(key), ChunkPos.getZ(key)), 0);
                outstandingTickets--;
                it.remove();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Phase 3 — Draw rows where all chunks are cached
    // ══════════════════════════════════════════════════════════════════════

    private boolean drawReadyRows(MapItemSavedData state) {
        long deadline = System.nanoTime() + BUDGET_NANOS;
        boolean madeProgress = false;

        while (currentRow < 128 && System.nanoTime() < deadline) {
            int rowZ  = (centerZ / i + currentRow - 64) * i;
            int minCZ = rowZ >> 4;
            int maxCZ = (rowZ + i - 1) >> 4;

            // Check if ALL chunks for this row are cached
            boolean allReady = true;
            for (int cx = minChunkX; cx <= maxChunkX && allReady; cx++) {
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    if (!chunkCache.containsKey(ChunkPos.pack(cx, cz))) {
                        allReady = false;
                        break;
                    }
                }
            }

            if (!allReady) break;

            // All chunks ready — render this row
            drawRow(state, rowZ);
            checkBannersInRow(state, rowZ);
            scanStructuresInRow(minCZ, maxCZ);

            if (currentRow >= 0) {
                state.setDirty();
            }

            currentRow++;
            madeProgress = true;
        }

        return madeProgress;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Row rendering — exact vanilla MapItem.update() parity
    // ══════════════════════════════════════════════════════════════════════

    private void drawRow(MapItemSavedData state, int rowZ) {
        // Pixel data arrays
        final MapColor.Brightness[] brightnesses = new MapColor.Brightness[128];
        final MapColor[]            colors       = new MapColor[128];

        if (i >= (1 << MIN_PARALLEL_SCALE)) {
            // ── Parallel path ────────────────────────────────────────────
            int segments = (127 + PARALLEL_SEGMENT_SIZE) / PARALLEL_SEGMENT_SIZE;
            List<CompletableFuture<Void>> futures = new ArrayList<>(segments);

            for (int seg = 0; seg < segments; seg++) {
                int colStart = seg * PARALLEL_SEGMENT_SIZE;
                int colEnd   = Math.min(colStart + PARALLEL_SEGMENT_SIZE, 128);
                futures.add(CompletableFuture.runAsync(() -> {
                    for (int col = colStart; col < colEnd; col++) {
                        computePixel(col, rowZ, colors, brightnesses);
                    }
                }, COMPUTE_POOL));
            }
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        } else {
            // ── Sequential path ──────────────────────────────────────────
            for (int col = 0; col < 128; col++) {
                computePixel(col, rowZ, colors, brightnesses);
            }
        }

        // Write pixel data to the map (shadow row -1 is skipped)
        if (currentRow >= 0) {
            for (int col = 0; col < 128; col++) {
                MapColor       mapColor   = colors[col];
                MapColor.Brightness bright = brightnesses[col];
                if (mapColor != MapColor.NONE) {
                    state.setColor(col, currentRow, mapColor.getPackedId(bright));
                }
            }
        }
    }

    private void computePixel(int col, int rowZ, MapColor[] colors, MapColor.Brightness[] brightnesses) {
        int mapX = (centerX / i + col - 64) * i;

        // ── Ceiling dimension: vanilla hash-based dirt/stone pattern ─────
        if (hasCeiling) {
            // Vanilla formula: u = (r + s * 231871); u = u*u*31287121 + u*11
            int u = mapX + rowZ * 231871;
            u = u * u * 31287121 + u * 11;
            if ((u >> 20 & 1) == 0) {
                colors[col] = Blocks.DIRT.defaultBlockState().getMapColor(world, BlockPos.ZERO);
            } else {
                colors[col] = Blocks.STONE.defaultBlockState().getMapColor(world, BlockPos.ZERO);
            }

            // Vanilla uses a constant e=100 for ceiling, making all visible rows NORMAL brightness
            double currentHeight = 100.0;
            double prevHeight    = prevColumnHeights[col];
            prevColumnHeights[col] = currentHeight;

            // Vanilla dither: ((o + p & 1) - 0.5) * 0.4  →  {-0.2, +0.2}
            double diff = (currentHeight - prevHeight) * 4.0 / (i + 4)
                    + ((col + currentRow & 1) - 0.5) * 0.4;
            if (diff > 0.6) {
                brightnesses[col] = MapColor.Brightness.HIGH;
            } else if (diff < -0.6) {
                brightnesses[col] = MapColor.Brightness.LOW;
            } else {
                brightnesses[col] = MapColor.Brightness.NORMAL;
            }
            return;
        }

        // ── Normal/overworld dimension rendering ─────────────────────────
        Multiset<MapColor> colorBag = LinkedHashMultiset.create();
        double currentHeight = 0.0;
        int    totalWaterDepth = 0;   // accumulated across all i*i sub-blocks

        for (int dx = 0; dx < i; dx++) {
            for (int dz = 0; dz < i; dz++) {
                int blockX = mapX + dx;
                int blockZ = rowZ + dz;
                int chunkX = blockX >> 4;
                int chunkZ_ = blockZ >> 4;

                ChunkAccess chunk = chunkCache.get(ChunkPos.pack(chunkX, chunkZ_));
                if (chunk == null) {
                    colorBag.add(MapColor.NONE);
                    continue;
                }

                int localX = blockX & 15;
                int localZ = blockZ & 15;

                int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, localX, localZ) + 1;
                BlockState blockState;

                if (y > world.getMinY()) {
                    // Walk down from surface to find non-air
                    do {
                        y--;
                        blockState = chunk.getBlockState(new BlockPos(blockX, y, blockZ));
                    } while (blockState.getMapColor(world, new BlockPos(blockX, y, blockZ)) == MapColor.NONE && y > world.getMinY());

                    // Fluid correction — vanilla getCorrectStateForFluidBlock
                    if (y > world.getMinY() && !blockState.getFluidState().isEmpty()) {
                        int fluidY = y - 1;
                        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                        BlockState below;
                        do {
                            mutablePos.set(blockX, fluidY--, blockZ);
                            below = chunk.getBlockState(mutablePos);
                            totalWaterDepth++;  // accumulate per sub-block (divided by i*i below)
                        } while (fluidY > world.getMinY() && !below.getFluidState().isEmpty());
                        blockState = getCorrectFluidBlock(world, blockState, new BlockPos(blockX, y, blockZ));
                    }
                } else {
                    blockState = Blocks.BEDROCK.defaultBlockState();
                }

                currentHeight += (double) y / (double) (i * i);
                colorBag.add(blockState.getMapColor(world, new BlockPos(blockX, y, blockZ)));
            }
        }

        // Majority color
        MapColor mapColor = Iterables.getFirst(
                Multisets.copyHighestCountFirst(colorBag), MapColor.NONE);
        colors[col] = mapColor;

        // Brightness / shadow computation
        if (mapColor == MapColor.WATER) {
            // Vanilla averages water depth over i*i sub-blocks: t /= i * i
            int waterDepth = totalWaterDepth / (i * i);
            double f = (double) waterDepth * 0.1 + (double) (col + currentRow & 1) * 0.2;
            if (f < 0.5) {
                brightnesses[col] = MapColor.Brightness.HIGH;
            } else if (f > 0.9) {
                brightnesses[col] = MapColor.Brightness.LOW;
            } else {
                brightnesses[col] = MapColor.Brightness.NORMAL;
            }
        } else {
            double prevHeight    = prevColumnHeights[col];
            // Store as double — truncating to int loses fractional precision and
            // produces incorrect diffs on gentle slopes.
            prevColumnHeights[col] = currentHeight;

            // Vanilla dither: ((o + p & 1) - 0.5F) * 0.4  →  {-0.2, +0.2}
            // The old code used (bit * 0.2) → {0, +0.2}, which biased bright slopes
            // toward HIGH instead of the correct dithered NORMAL/HIGH mix.
            double diff = (currentHeight - prevHeight) * 4.0 / (i + 4)
                    + ((col + currentRow & 1) - 0.5) * 0.4;

            if (diff > 0.6) {
                brightnesses[col] = MapColor.Brightness.HIGH;
            } else if (diff < -0.6) {
                brightnesses[col] = MapColor.Brightness.LOW;
            } else {
                brightnesses[col] = MapColor.Brightness.NORMAL;
            }
        }
    }

    /**
     * Vanilla fluid block correction (MapItem.getCorrectStateForFluidBlock).
     */
    private static BlockState getCorrectFluidBlock(ServerLevel world, BlockState state, BlockPos pos) {
        FluidState fluidState = state.getFluidState();
        return !fluidState.isEmpty() && !state.isFaceSturdy(world, pos, Direction.UP)
                ? fluidState.createLegacyBlock()
                : state;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Banner checking — vanilla parity
    // ══════════════════════════════════════════════════════════════════════

    private void checkBannersInRow(MapItemSavedData state, int rowZ) {
        if (currentRow < 0) return; // shadow row, skip

        for (int col = 0; col < 128; col++) {
            int mapX = (centerX / i + col - 64) * i;
            for (int dx = 0; dx < i; dx++) {
                for (int dz = 0; dz < i; dz++) {
                    int blockX = mapX + dx;
                    int blockZ = rowZ + dz;
                    state.checkBanners(world, blockX, blockZ);
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Structure detection
    // ══════════════════════════════════════════════════════════════════════

    private void scanStructuresInRow(int minCZ, int maxCZ) {
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                ChunkAccess chunk = chunkCache.get(ChunkPos.pack(cx, cz));
                if (chunk == null) continue;

                Map<Structure, StructureStart> starts = chunk.getAllStarts();
                if (starts.isEmpty()) continue;

                Registry<@NotNull Structure> registry =
                        world.registryAccess().lookupOrThrow(Registries.STRUCTURE);

                for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
                    StructureStart start = entry.getValue();
                    if (start == null || !start.isValid()) continue;

                    Identifier structureId = registry.getKey(entry.getKey());
                    if (structureId == null) continue;

                    // Check if path (e.g. "mansion") matches a known icon
                    Identifier pathOnly = Identifier.withDefaultNamespace(structureId.getPath());

                    Holder<@NotNull MapDecorationType> iconHolder = STRUCTURE_ICONS.get(pathOnly);
                    if (iconHolder == null) continue;
                    if (detectedStructures.contains(pathOnly)) continue;
                    detectedStructures.add(pathOnly);

                    // Use the bounding box center as the icon position
                    var bb = start.getBoundingBox();
                    int iconX = (bb.minX() + bb.maxX()) / 2;
                    int iconZ = (bb.minZ() + bb.maxZ()) / 2;

                    String decoKey = "neatlybetter_" + structureId.getPath()
                            + "_" + iconX + "_" + iconZ;
                    structureIcons.put(decoKey, new StructureIconEntry(iconHolder, iconX, iconZ, structureId.getPath()));
                }
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void applyStructureDecorations(MapItemSavedData state) {
        if (structureIcons.isEmpty()) return;

        for (Map.Entry<String, StructureIconEntry> e : structureIcons.entrySet()) {
            StructureIconEntry icon = e.getValue();
            state.addDecoration(
                    icon.type(), world, e.getKey(),
                    icon.x(), icon.z(), 180.0, null);
        }

        MapId mapId = mapStack.get(DataComponents.MAP_ID);
        if (mapId != null) {
            List<NTMapData.StructureEntry> persistent = new ArrayList<>();
            for (Map.Entry<String, StructureIconEntry> e : structureIcons.entrySet()) {
                StructureIconEntry icon = e.getValue();
                persistent.add(new NTMapData.StructureEntry(
                        e.getKey(), icon.structurePath(), icon.x(), icon.z()));
            }
            NTMapData data = NTMapData.get(world.getServer());
            data.putStructures(mapId.key(), persistent);
            data.markRestored(mapId.key());
        }

        state.setDirty();
    }

    private record StructureIconEntry(Holder<@NotNull MapDecorationType> type, int x, int z, String structurePath) {}

    // ══════════════════════════════════════════════════════════════════════
    //  Server-start restoration
    // ══════════════════════════════════════════════════════════════════════

    public static void restoreAllStructuresIcons(MinecraftServer server) {
        NTMapData mapData = NTMapData.get(server);

        for (String mapKey : mapData.getAllMapKeys()) {
            if (!mapData.needsRestore(mapKey)) continue;

            MapId mapId;
            try {
                int id = Integer.parseInt(mapKey.substring("map_".length()));
                mapId = new MapId(id);
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                continue;
            }

            MapItemSavedData state = server.overworld().getMapData(mapId);
            if (state == null) continue;

            for (NTMapData.StructureEntry entry : mapData.getStructures(mapKey)) {
                Identifier pathOnly = Identifier.withDefaultNamespace(entry.structurePath());
                Holder<@NotNull MapDecorationType> holder = STRUCTURE_ICONS.get(pathOnly);
                if (holder == null) continue;

                state.addDecoration(
                        holder, null, entry.decoKey(),
                        entry.x(), entry.z(), 180.0, null);
            }

            mapData.markRestored(mapKey);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Memory management
    // ══════════════════════════════════════════════════════════════════════

    private void evictProcessedChunks() {
        if (currentRow <= 0) return;

        // Compute the minimum chunk Z still needed
        int prevRowZ = (centerZ / i + (currentRow - 1) - 64) * i;
        int evictBelowCZ = prevRowZ >> 4;

        chunkCache.entrySet().removeIf(entry ->
                ChunkPos.getZ(entry.getKey()) < evictBelowCZ);
    }

    private static boolean isMemoryPressured() {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        long max  = rt.maxMemory();
        return (double) used / max > MEMORY_PRESSURE_THRESHOLD;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Cleanup
    // ══════════════════════════════════════════════════════════════════════

    private void cleanup() {
        if (!ticketedPositions.isEmpty()) {
            ServerChunkCache cs = world.getChunkSource();
            for (long key : ticketedPositions) {
                cs.removeTicketWithRadius(ModTickets.MAP_SCAN,
                        new ChunkPos(ChunkPos.getX(key), ChunkPos.getZ(key)), 0);
            }
            ticketedPositions.clear();
        }
        chunkCache.clear();
        outstandingTickets = 0;
    }
}
