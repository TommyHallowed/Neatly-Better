package net.hallowed.neatlybetter.feature.map;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.feature.map.NTMapData;
import net.hallowed.neatlybetter.registry.ModTickets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Fills every map of one map-builder job with vanilla {@code MapItem.update} parity.
 *
 * <ul>
 *   <li>Chunks are only generated/loaded to {@code FEATURES} (no light, spawn or FULL promotion).
 *       A chunk's blocks are final once it and its 8 neighbors reached FEATURES, so the target area
 *       plus a 1-chunk ring is ticketed.</li>
 *   <li>Each chunk is scanned once, as soon as it is ready, on {@link #COMPUTE_POOL}; workers only read the
 *       {@link ChunkAccess} (never the {@link ServerLevel}), and the main thread never joins them.</li>
 *   <li>Per-pixel results (color, average height, water depth) go into flat arrays; shading needs the pixel to
 *       the north, so it is applied in one pass when all chunks of a map are done.</li>
 *   <li>All maps of a job share one ticket window, so maps of a wall scan concurrently and shared border
 *       chunks load once.</li>
 * </ul>
 */
public class FastChunkScanner implements MapBuilderItem.MapGenerationTask {

    private static final Logger LOGGER = LoggerFactory.getLogger("neatly-better/map-scanner");

    private static final ForkJoinPool COMPUTE_POOL = new ForkJoinPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            pool -> {
                ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 2);
                t.setName("NeatlyBetter-MapWorker-" + t.getPoolIndex());
                return t;
            },
            null, true
    );

    // ──────────────────────────────────────────────────────────────────────
    //  Tuning
    // ──────────────────────────────────────────────────────────────────────

    /** {@code addTicketWithRadius} radius that yields the FEATURES ticket level (negative: below FULL). */
    private static final int FEATURES_RADIUS =
            ChunkLevel.byStatus(FullChunkStatus.FULL) - ChunkLevel.byStatus(ChunkStatus.FEATURES);

    /** Target chunks requested but not yet scanned. Bounds worldgen load, scales with CPU count. */
    private static final int MAX_PENDING_TARGETS =
            Math.clamp(Runtime.getRuntime().availableProcessors() * 4L, 16, 256);

    /** Chunk scans running on {@link #COMPUTE_POOL} at once. */
    private static final int MAX_IN_FLIGHT_SCANS = Math.max(4, Runtime.getRuntime().availableProcessors());

    /** Main-thread time budget per tick. */
    private static final long BUDGET_NANOS = 5_000_000L;

    /** Heap usage above which no new tickets are added. */
    private static final double MEMORY_PRESSURE_THRESHOLD = 0.85;

    // ──────────────────────────────────────────────────────────────────────
    //  Structure → map-icon mapping
    // ──────────────────────────────────────────────────────────────────────

    private static final Map<Identifier, Holder<@NotNull MapDecorationType>> STRUCTURE_ICONS = Map.of(
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

    // ══════════════════════════════════════════════════════════════════════
    //  Instance state (main thread only, except MapState pixel arrays)
    // ══════════════════════════════════════════════════════════════════════

    private final ServerLevel world;
    private final int minY;
    private final boolean hasCeiling;
    private final List<MapState> maps = new ArrayList<>();

    private boolean started = false;
    private int totalTargets = 0;
    private int doneTargets = 0;
    private int finishedMaps = 0;

    /** Map whose chunks are currently being queued, and the position in its ordered chunk list. */
    private int queueMapIndex = 0;
    private long[] queueChunks = null;
    private int queueChunkIndex = 0;

    /** Every target chunk ever queued (avoids queueing chunks shared by two maps twice). */
    private final LongOpenHashSet queuedTargets = new LongOpenHashSet();
    /** Target chunks that finished scanning. */
    private final LongOpenHashSet scannedTargets = new LongOpenHashSet();
    /** Chunks holding a MAP_SCAN ticket (targets and their ring). */
    private final LongOpenHashSet ticketed = new LongOpenHashSet();
    /** Queued targets whose 3×3 is ticketed, waiting for generation, in queue order. */
    private final LongArrayList waiting = new LongArrayList();
    private final List<InFlightScan> inFlight = new ArrayList<>();

    private record InFlightScan(long key, CompletableFuture<Void> future) {}

    public FastChunkScanner(ServerLevel world) {
        this.world = world;
        this.minY = world.getMinY();
        this.hasCeiling = world.dimensionType().hasCeiling();
    }

    /**
     * Adds a map to this job. Maps are filled in the order they are added (overlapping in time).
     * {@code frame} (nullable) holds the placed copy of the map; structure icons are stored on its item.
     */
    public void addMap(ItemStack mapStack, ItemFrame frame, int centerX, int centerZ, int zoom) {
        if (started) throw new IllegalStateException("Scanner already started");
        MapState map = new MapState(mapStack, centerX, centerZ, 1 << zoom);
        map.frame = frame;
        maps.add(map);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MapGenerationTask API
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public boolean process() {
        if (!started) start();

        long deadline = System.nanoTime() + BUDGET_NANOS;
        ServerChunkCache cs = world.getChunkSource();

        collectFinishedScans(cs, deadline);
        if (!isMemoryPressured()) submitTickets(cs);
        dispatchReadyChunks(cs, deadline);

        if (finishedMaps >= maps.size()) {
            cleanup();
            return true;
        }
        return false;
    }

    /** One {@link #process()} call per tick is enough; the work runs on worldgen and compute threads. */
    @Override
    public boolean isWaiting() {
        return true;
    }

    @Override
    public void cancel() {
        cleanup();
    }

    @Override
    public float getProgress() {
        if (totalTargets == 0) return maps.isEmpty() ? 1f : (float) finishedMaps / maps.size();
        return (float) doneTargets / totalTargets;
    }

    private void start() {
        started = true;
        // Ceiling dimensions need no chunk data (vanilla draws a hash pattern there).
        if (hasCeiling) {
            for (MapState map : maps) {
                map.fillCeiling();
                finishMap(map);
            }
            return;
        }
        // Chunks shared by two maps (one map's shadow row) are scanned once, so count distinct ones.
        LongOpenHashSet all = new LongOpenHashSet();
        for (MapState map : maps) {
            map.remainingChunks = map.chunkXs.length * map.chunkZs.length;
            for (int cz : map.chunkZs) for (int cx : map.chunkXs) all.add(ChunkPos.pack(cx, cz));
        }
        totalTargets = all.size();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Tickets
    // ══════════════════════════════════════════════════════════════════════

    private void submitTickets(ServerChunkCache cs) {
        if (hasCeiling) return;
        LongArrayList added = new LongArrayList();

        while (waiting.size() + inFlight.size() < MAX_PENDING_TARGETS) {
            long target = nextQueuedTarget();
            if (target == Long.MIN_VALUE) break;

            int cx = ChunkPos.getX(target), cz = ChunkPos.getZ(target);
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    long key = ChunkPos.pack(cx + dx, cz + dz);
                    if (ticketed.add(key)) {
                        cs.addTicketWithRadius(ModTickets.MAP_SCAN, new ChunkPos(cx + dx, cz + dz), FEATURES_RADIUS);
                        added.add(key);
                    }
                }
            }
            waiting.add(target);
        }

        if (added.isEmpty()) return;
        cs.runDistanceManagerUpdates();
        // Below FULL a ticket only keeps a chunk loadable; generation/loading must be requested explicitly
        // (vanilla does this in ServerChunkCache.getChunkFutureMainThread). Non-blocking.
        for (int k = 0; k < added.size(); k++) {
            ChunkHolder holder = cs.chunkMap.getUpdatingChunkIfPresent(added.getLong(k));
            if (holder != null) holder.scheduleChunkGenerationTask(ChunkStatus.FEATURES, cs.chunkMap);
        }
    }

    /** Next target chunk in queue order (map by map, region-file order inside a map), or MIN_VALUE. */
    private long nextQueuedTarget() {
        while (queueMapIndex < maps.size()) {
            if (queueChunks == null) {
                queueChunks = maps.get(queueMapIndex).orderedChunks();
                queueChunkIndex = 0;
            }
            while (queueChunkIndex < queueChunks.length) {
                long key = queueChunks[queueChunkIndex++];
                if (queuedTargets.add(key)) return key;
            }
            queueChunks = null;
            queueMapIndex++;
        }
        return Long.MIN_VALUE;
    }

    /** Releases the tickets around a finished target that no unscanned target still needs. */
    private void releaseAround(ServerChunkCache cs, long target) {
        int cx = ChunkPos.getX(target), cz = ChunkPos.getZ(target);
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                long key = ChunkPos.pack(cx + dx, cz + dz);
                if (ticketed.contains(key) && !neededByUnscannedTarget(cx + dx, cz + dz)) {
                    ticketed.remove(key);
                    cs.removeTicketWithRadius(ModTickets.MAP_SCAN, new ChunkPos(cx + dx, cz + dz), FEATURES_RADIUS);
                }
            }
        }
    }

    private boolean neededByUnscannedTarget(int cx, int cz) {
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = cx + dx, z = cz + dz;
                if (isTarget(x, z) && !scannedTargets.contains(ChunkPos.pack(x, z))) return true;
            }
        }
        return false;
    }

    private boolean isTarget(int cx, int cz) {
        for (MapState map : maps) {
            if (map.coversChunk(cx, cz)) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Readiness and dispatch
    // ══════════════════════════════════════════════════════════════════════

    private static ChunkAccess featuresChunk(ServerChunkCache cs, long key) {
        ChunkHolder holder = cs.chunkMap.getUpdatingChunkIfPresent(key);
        return holder == null ? null : holder.getChunkIfPresent(ChunkStatus.FEATURES);
    }

    private boolean isReady(ServerChunkCache cs, int cx, int cz) {
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                if ((dx | dz) != 0 && featuresChunk(cs, ChunkPos.pack(cx + dx, cz + dz)) == null) return false;
            }
        }
        return true;
    }

    private void dispatchReadyChunks(ServerChunkCache cs, long deadline) {
        int write = 0;
        int size = waiting.size();
        for (int read = 0; read < size; read++) {
            long key = waiting.getLong(read);
            boolean dispatched = false;

            if (inFlight.size() < MAX_IN_FLIGHT_SCANS && System.nanoTime() < deadline) {
                ChunkAccess chunk = featuresChunk(cs, key);
                int cx = ChunkPos.getX(key), cz = ChunkPos.getZ(key);
                if (chunk != null && isReady(cs, cx, cz)) {
                    dispatch(key, cx, cz, chunk);
                    dispatched = true;
                }
            }
            if (!dispatched) waiting.set(write++, key);
        }
        waiting.size(write);
    }

    private void dispatch(long key, int cx, int cz, ChunkAccess chunk) {
        List<MapState> interested = new ArrayList<>(2);
        for (MapState map : maps) {
            if (map.coversChunk(cx, cz)) interested.add(map);
        }
        scanStructures(chunk, cx, cz, interested);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            PixelScratch scratch = new PixelScratch();
            for (MapState map : interested) map.scanChunk(chunk, cx, cz, minY, scratch);
        }, COMPUTE_POOL);
        inFlight.add(new InFlightScan(key, future));
    }

    private void collectFinishedScans(ServerChunkCache cs, long deadline) {
        Iterator<InFlightScan> it = inFlight.iterator();
        while (it.hasNext()) {
            InFlightScan scan = it.next();
            if (!scan.future().isDone()) continue;
            if (scan.future().isCompletedExceptionally()) {
                try {
                    scan.future().join();
                } catch (Exception e) {
                    LOGGER.error("Map chunk scan failed at {}", new ChunkPos(ChunkPos.getX(scan.key()), ChunkPos.getZ(scan.key())), e);
                }
            }
            it.remove();

            long key = scan.key();
            int cx = ChunkPos.getX(key), cz = ChunkPos.getZ(key);
            scannedTargets.add(key);
            doneTargets++;
            releaseAround(cs, key);

            for (MapState map : maps) {
                if (map.coversChunk(cx, cz) && --map.remainingChunks == 0) finishMap(map);
            }
            if (System.nanoTime() >= deadline) break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Finishing a map — shading pass, banners, structure icons
    // ══════════════════════════════════════════════════════════════════════

    private void finishMap(MapState map) {
        finishedMaps++;
        MapId mapId = map.mapStack.get(DataComponents.MAP_ID);
        if (mapId == null) return;
        MapItemSavedData state = world.getMapData(mapId);
        if (state == null) return;

        map.applyColors(state);
        checkBanners(state, map);
        applyStructureDecorations(state, map, mapId);
        state.setDirty();
    }

    /** Vanilla checks banners per sampled block; doing it once per marker inside the map area is equivalent. */
    private void checkBanners(MapItemSavedData state, MapState map) {
        if (state.getBanners().isEmpty()) return;
        int minX = map.baseX * map.i, maxX = (map.baseX + 128) * map.i;
        int minZ = map.baseZ * map.i, maxZ = (map.baseZ + 128) * map.i;
        for (MapBanner banner : new ArrayList<>(state.getBanners())) {
            int x = banner.pos().getX(), z = banner.pos().getZ();
            if (x >= minX && x < maxX && z >= minZ && z < maxZ) state.checkBanners(world, x, z);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Structure detection (main thread, once per chunk)
    // ══════════════════════════════════════════════════════════════════════

    private void scanStructures(ChunkAccess chunk, int cx, int cz, List<MapState> interested) {
        Map<Structure, StructureStart> starts = chunk.getAllStarts();
        if (starts.isEmpty()) return;

        Registry<@NotNull Structure> registry = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
            StructureStart start = entry.getValue();
            if (start == null || !start.isValid()) continue;

            Identifier structureId = registry.getKey(entry.getKey());
            if (structureId == null) continue;

            Identifier pathOnly = Identifier.withDefaultNamespace(structureId.getPath());
            Holder<@NotNull MapDecorationType> iconHolder = STRUCTURE_ICONS.get(pathOnly);
            if (iconHolder == null) continue;

            var bb = start.getBoundingBox();
            int iconX = (bb.minX() + bb.maxX()) / 2;
            int iconZ = (bb.minZ() + bb.maxZ()) / 2;

            for (MapState map : interested) {
                if (!map.drawsChunkRow(cz) || !map.detectedStructures.add(pathOnly)) continue;
                String decoKey = "neatlybetter_" + structureId.getPath() + "_" + iconX + "_" + iconZ;
                map.structureIcons.put(decoKey, new StructureIconEntry(iconHolder, iconX, iconZ, structureId.getPath()));
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void applyStructureDecorations(MapItemSavedData state, MapState map, MapId mapId) {
        if (map.structureIcons.isEmpty()) return;

        List<NTMapData.StructureEntry> persistent = new ArrayList<>();
        Map<String, MapDecorations.Entry> component = new LinkedHashMap<>();
        for (Map.Entry<String, StructureIconEntry> e : map.structureIcons.entrySet()) {
            StructureIconEntry icon = e.getValue();
            state.addDecoration(icon.type(), world, e.getKey(), icon.x(), icon.z(), 180.0, null);
            persistent.add(new NTMapData.StructureEntry(e.getKey(), icon.structurePath(), icon.x(), icon.z()));
            component.put(e.getKey(), new MapDecorations.Entry(icon.type(), icon.x(), icon.z(), 180.0f));
        }

        // Like explorer maps: decorations on the item are re-added by vanilla every time the map is ticked
        // (held or framed), so they survive reloads and copies. Map decorations themselves are not saved.
        MapDecorations decorations = new MapDecorations(component);
        map.mapStack.set(DataComponents.MAP_DECORATIONS, decorations);
        if (map.frame != null && !map.frame.isRemoved()) {
            ItemStack framed = map.frame.getItem();
            if (mapId.equals(framed.get(DataComponents.MAP_ID))) {
                framed.set(DataComponents.MAP_DECORATIONS, decorations);
            }
        }
        NTMapData data = NTMapData.get(world.getServer());
        data.putStructures(mapId.key(), persistent);
        data.markRestored(mapId.key());
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

                state.addDecoration(holder, null, entry.decoKey(), entry.x(), entry.z(), 180.0, null);
            }

            mapData.markRestored(mapKey);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Memory / cleanup
    // ══════════════════════════════════════════════════════════════════════

    private static boolean isMemoryPressured() {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        return (double) used / rt.maxMemory() > MEMORY_PRESSURE_THRESHOLD;
    }

    private void cleanup() {
        if (!ticketed.isEmpty()) {
            ServerChunkCache cs = world.getChunkSource();
            for (long key : ticketed) {
                cs.removeTicketWithRadius(ModTickets.MAP_SCAN,
                        new ChunkPos(ChunkPos.getX(key), ChunkPos.getZ(key)), FEATURES_RADIUS);
            }
            ticketed.clear();
        }
        // Running scans only read their chunk and write their own map's arrays; let them finish and drop them.
        inFlight.clear();
        waiting.clear();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Per-map state and pixel work
    // ══════════════════════════════════════════════════════════════════════

    /** Reused per worker task: color histogram with first-seen order (vanilla tie-break). */
    private static final class PixelScratch {
        final int[] counts = new int[64];
        final int[] seen = new int[64];
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
    }

    private static final class MapState {
        final ItemStack mapStack;
        final int i;
        /** Pixel (col,row) covers blocks starting at ((baseX+col)·i, (baseZ+row)·i), like vanilla. */
        final int baseX, baseZ;
        /** Chunk of each column's / row's first block; rows include the shadow row -1 at index 0. */
        final int[] colChunk = new int[128];
        final int[] rowChunk = new int[129];
        /** Distinct chunk coordinates covered (ascending). */
        final int[] chunkXs, chunkZs;

        /** Pixel arrays indexed (row+1)·128 + col, row ∈ [-1, 127]. Written by workers, read after completion. */
        final byte[]   colorIds = new byte[128 * 129];
        final double[] heights  = new double[128 * 129];
        final int[]    water    = new int[128 * 129];

        int remainingChunks;
        ItemFrame frame;
        final Set<Identifier> detectedStructures = new HashSet<>();
        final Map<String, StructureIconEntry> structureIcons = new LinkedHashMap<>();

        MapState(ItemStack mapStack, int centerX, int centerZ, int i) {
            this.mapStack = mapStack;
            this.i = i;
            this.baseX = centerX / i - 64;
            this.baseZ = centerZ / i - 64;
            for (int col = 0; col < 128; col++) colChunk[col] = ((baseX + col) * i) >> 4;
            for (int row = -1; row < 128; row++) rowChunk[row + 1] = ((baseZ + row) * i) >> 4;
            this.chunkXs = Arrays.stream(colChunk).distinct().toArray();
            this.chunkZs = Arrays.stream(rowChunk).distinct().toArray();
        }

        boolean coversChunk(int cx, int cz) {
            return cx >= chunkXs[0] && cx <= chunkXs[chunkXs.length - 1]
                    && cz >= chunkZs[0] && cz <= chunkZs[chunkZs.length - 1];
        }

        /** True when the chunk row holds at least one drawn (non-shadow) pixel row. */
        boolean drawsChunkRow(int cz) {
            return cz >= rowChunk[1] && cz <= rowChunk[128];
        }

        /** Chunks sorted by region file (32×32), then row-major, so disk reads stay mostly sequential. */
        long[] orderedChunks() {
            long[] out = new long[chunkXs.length * chunkZs.length];
            int n = 0;
            for (int cz : chunkZs) for (int cx : chunkXs) out[n++] = ChunkPos.pack(cx, cz);
            Long[] boxed = Arrays.stream(out).boxed().toArray(Long[]::new);
            Arrays.sort(boxed, Comparator
                    .comparingInt((Long k) -> ChunkPos.getZ(k) >> 5)
                    .thenComparingInt(k -> ChunkPos.getX(k) >> 5)
                    .thenComparingInt(ChunkPos::getZ)
                    .thenComparingInt(ChunkPos::getX));
            for (int j = 0; j < out.length; j++) out[j] = boxed[j];
            return out;
        }

        /** Worker thread: computes every pixel whose first block lies in this chunk. Reads only {@code chunk}. */
        void scanChunk(ChunkAccess chunk, int cx, int cz, int minY, PixelScratch s) {
            for (int row = -1; row < 128; row++) {
                if (rowChunk[row + 1] != cz) continue;
                for (int col = 0; col < 128; col++) {
                    if (colChunk[col] != cx) continue;
                    computePixel(chunk, col, row, minY, s);
                }
            }
        }

        /** Exact port of the per-pixel part of vanilla {@code MapItem.update}. */
        private void computePixel(ChunkAccess chunk, int col, int row, int minY, PixelScratch s) {
            int minX = (baseX + col) * i;
            int minZ = (baseZ + row) * i;
            BlockPos.MutableBlockPos pos = s.pos;
            BlockPos.MutableBlockPos below = s.below;
            int[] counts = s.counts;
            int[] seen = s.seen;
            int seenCount = 0;

            int waterDepth = 0;
            double height = 0.0;

            for (int dx = 0; dx < i; dx++) {
                for (int dz = 0; dz < i; dz++) {
                    pos.set(minX + dx, 0, minZ + dz);
                    int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) + 1;
                    BlockState state;
                    if (y <= minY) {
                        state = Blocks.BEDROCK.defaultBlockState();
                    } else {
                        do {
                            pos.setY(--y);
                            state = chunk.getBlockState(pos);
                        } while (state.getMapColor(chunk, pos) == MapColor.NONE && y > minY);

                        if (y > minY && !state.getFluidState().isEmpty()) {
                            int solidY = y - 1;
                            below.set(pos);
                            BlockState belowBlock;
                            do {
                                below.setY(solidY--);
                                belowBlock = chunk.getBlockState(below);
                                waterDepth++;
                            } while (solidY > minY && !belowBlock.getFluidState().isEmpty());
                            state = correctFluidBlock(chunk, state, pos);
                        }
                    }

                    height += (double) y / (i * i);
                    int id = state.getMapColor(chunk, pos).id;
                    if (counts[id]++ == 0) seen[seenCount++] = id;
                }
            }

            // Majority color; ties go to the color seen first (LinkedHashMultiset + copyHighestCountFirst).
            int best = MapColor.NONE.id, bestCount = 0;
            for (int k = 0; k < seenCount; k++) {
                int id = seen[k];
                if (counts[id] > bestCount) { best = id; bestCount = counts[id]; }
                counts[id] = 0;
            }

            int idx = (row + 1) * 128 + col;
            colorIds[idx] = (byte) best;
            heights[idx] = height;
            water[idx] = waterDepth / (i * i);
        }

        /** Vanilla {@code MapItem.getCorrectStateForFluidBlock}, against the chunk (never the level, see B1). */
        private static BlockState correctFluidBlock(BlockGetter getter, BlockState state, BlockPos pos) {
            FluidState fluidState = state.getFluidState();
            return !fluidState.isEmpty() && !state.isFaceSturdy(getter, pos, Direction.UP)
                    ? fluidState.createLegacyBlock()
                    : state;
        }

        /** Vanilla hash pattern for ceiling dimensions, constant height 100. */
        void fillCeiling() {
            int dirt = MapColor.DIRT.id, stone = MapColor.STONE.id;
            for (int row = -1; row < 128; row++) {
                for (int col = 0; col < 128; col++) {
                    int u = (baseX + col) * i + (baseZ + row) * i * 231871;
                    u = u * u * 31287121 + u * 11;
                    int idx = (row + 1) * 128 + col;
                    colorIds[idx] = (byte) ((u >> 20 & 1) == 0 ? dirt : stone);
                    heights[idx] = 100.0;
                }
            }
        }

        /** Main thread: vanilla shading against the pixel to the north, then writes the map. */
        void applyColors(MapItemSavedData state) {
            for (int row = 0; row < 128; row++) {
                for (int col = 0; col < 128; col++) {
                    int idx = (row + 1) * 128 + col;
                    MapColor color = MapColor.byId(colorIds[idx] & 0xFF);
                    if (color == MapColor.NONE) continue;

                    int dither = col + row & 1;
                    double diff;
                    MapColor.Brightness brightness;
                    if (color == MapColor.WATER) {
                        diff = water[idx] * 0.1 + dither * 0.2;
                        brightness = diff < 0.5 ? MapColor.Brightness.HIGH
                                : diff > 0.9 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                    } else {
                        diff = (heights[idx] - heights[idx - 128]) * 4.0 / (i + 4) + (dither - 0.5) * 0.4;
                        brightness = diff > 0.6 ? MapColor.Brightness.HIGH
                                : diff < -0.6 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                    }
                    state.setColor(col, row, color.getPackedId(brightness));
                }
            }
        }
    }
}
