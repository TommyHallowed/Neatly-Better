package net.hallowed.oldways.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.hallowed.oldways.content.item.MapBuilderItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FastChunkScanner implements MapBuilderItem.MapGenerationTask {

    public static final TicketType MAP_BUILDER_TICKET = TicketType.PLAYER_LOADING;

    private final ServerLevel world;
    private final ItemStack mapStack;
    private final int centerZ;
    private final int i; // scale multiplier

    // Global Map X Boundaries (These never change)
    private final int minBlockX;
    private final int minChunkX;
    private final int maxChunkX;

    // --- STREAMING STATE TRACKER ---
    // 0 = Prepare Row Chunks, 1 = Wait for Row Chunks, 2 = Draw Row
    private int processState = 0;
    private int p = -1; // Current map row being drawn (-1 for shadow initialization)
    private int waitIndex = 0;

    private Set<ChunkPos> activeTickets = new HashSet<>();
    private List<ChunkPos> rowChunksList = new ArrayList<>();
    private final double[] previousHeights = new double[128];

    public FastChunkScanner(ServerLevel world, ItemStack mapStack, int centerX, int centerZ, int scale) {
        this.world = world;
        this.mapStack = mapStack;
        this.centerZ = centerZ;
        this.i = 1 << scale;

        // Calculate global X boundaries just once
        this.minBlockX = (centerX / i - 64) * i;
        int maxBlockX = (centerX / i + 63) * i + (i - 1);

        this.minChunkX = minBlockX >> 4;
        this.maxChunkX = maxBlockX >> 4;
    }

    @Override
    public boolean process() {
        if (processState == 0) {
            // STEP 0: PREPARE ROW CHUNKS (Memory Streaming)
            // Calculate exactly which chunks we need for JUST this one row of pixels
            int minBlockZ = (centerZ / i + p - 64) * i;
            int maxBlockZ = minBlockZ + (i - 1);

            int minChunkZ = minBlockZ >> 4;
            int maxChunkZ = maxBlockZ >> 4;

            Set<ChunkPos> neededThisRow = new HashSet<>();
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    neededThisRow.add(new ChunkPos(cx, cz));
                }
            }

            // CRUCIAL: Unload chunks that are safely behind us to free up gigabytes of RAM!
            for (ChunkPos pos : activeTickets) {
                if (!neededThisRow.contains(pos)) {
                    world.getChunkSource().removeTicketWithRadius(MAP_BUILDER_TICKET, pos, 1);
                }
            }

            // Load new chunks needed for this row
            for (ChunkPos pos : neededThisRow) {
                if (!activeTickets.contains(pos)) {
                    world.getChunkSource().addTicketWithRadius(MAP_BUILDER_TICKET, pos, 1);
                }
            }

            activeTickets = neededThisRow;
            rowChunksList = new ArrayList<>(neededThisRow);
            waitIndex = 0;
            processState = 1;
            return false; // Yield tick

        } else if (processState == 1) {
            // STEP 1: WAIT FOR ROW CHUNKS
            int toProcess = Math.min(20, rowChunksList.size() - waitIndex);
            int forcedThisTick = 0;

            for (int j = 0; j < toProcess; j++) {
                if (waitIndex >= rowChunksList.size()) break;

                ChunkPos pos = rowChunksList.get(waitIndex);
                ChunkAccess chunk = world.getChunkSource().getChunk(pos.x, pos.z, ChunkStatus.FEATURES, false);

                if (chunk == null) {
                    if (forcedThisTick >= 1) break; // Limit CPU lag by only forcing 1 per tick
                    world.getChunk(pos.x, pos.z, ChunkStatus.FEATURES, true);
                    forcedThisTick++;
                }
                waitIndex++;
            }

            if (waitIndex >= rowChunksList.size()) {
                processState = 2; // Row is fully loaded, time to draw!
            }
            return false;

        } else {
            // STEP 2: DRAW ROW (Ultra-Fast Array Caching)
            MapId mapId = mapStack.get(DataComponents.MAP_ID);
            if (mapId == null) return finish();
            MapItemSavedData state = world.getMapData(mapId);
            if (state == null) return finish();

            int startZ = (centerZ / i + p - 64) * i;
            int minChunkZ = startZ >> 4;
            int maxChunkZ = (startZ + i - 1) >> 4;

            // CACHE THE CHUNKS: Eliminates calling world.getChunk() 32,000 times!
            int cacheWidth = maxChunkX - minChunkX + 1;
            int cacheHeight = maxChunkZ - minChunkZ + 1;
            ChunkAccess[] chunkCache = new ChunkAccess[cacheWidth * cacheHeight];

            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    int idx = (cx - minChunkX) + (cz - minChunkZ) * cacheWidth;
                    chunkCache[idx] = world.getChunkSource().getChunk(cx, cz, ChunkStatus.FEATURES, false);
                }
            }

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos waterPos = new BlockPos.MutableBlockPos();
            int bottomY = world.getMinY();

            // Process all 128 horizontal pixels for this single row in one tick
            for (int o = 0; o < 128; o++) {
                Multiset<MapColor> multiset = LinkedHashMultiset.create();
                int waterDepth = 0;
                double currentHeight = 0.0;

                int startX = minBlockX + o * i;

                for (int u = 0; u < i; ++u) {
                    for (int v = 0; v < i; ++v) {
                        int blockX = startX + u;
                        int blockZ = startZ + v;

                        int cx = blockX >> 4;
                        int cz = blockZ >> 4;
                        int idx = (cx - minChunkX) + (cz - minChunkZ) * cacheWidth;
                        ChunkAccess chunk = chunkCache[idx];

                        if (chunk == null) continue;

                        int localX = blockX & 15;
                        int localZ = blockZ & 15;

                        int w = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, localX, localZ) + 1;
                        BlockState blockState;

                        if (w <= bottomY) {
                            blockState = Blocks.BEDROCK.defaultBlockState();
                        } else {
                            do {
                                --w;
                                pos.set(blockX, w, blockZ);
                                blockState = chunk.getBlockState(pos);
                            } while (blockState.getMapColor(world, pos) == MapColor.NONE && w > bottomY);

                            if (w > bottomY && !blockState.getFluidState().isEmpty()) {
                                int x = w - 1;
                                waterPos.set(pos);
                                BlockState waterState;
                                do {
                                    waterPos.setY(x--);
                                    waterState = chunk.getBlockState(waterPos);
                                    ++waterDepth;
                                } while (x > bottomY && !waterState.getFluidState().isEmpty());

                                FluidState fluidState = blockState.getFluidState();
                                if (!fluidState.isEmpty() && !blockState.isFaceSturdy(world, pos, Direction.UP)) {
                                    blockState = fluidState.createLegacyBlock();
                                }
                            }
                        }

                        currentHeight += (double) w / (double) (i * i);
                        multiset.add(blockState.getMapColor(world, pos));
                    }
                }

                waterDepth /= Math.max(1, i * i);
                MapColor mapColor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.NONE);

                MapColor.Brightness brightness = MapColor.Brightness.NORMAL;
                if (mapColor == MapColor.WATER) {
                    double depthVisual = (double) waterDepth * 0.1 + (double) (o + p & 1) * 0.2;
                    if (depthVisual < 0.5) brightness = MapColor.Brightness.HIGH;
                    else if (depthVisual > 0.9) brightness = MapColor.Brightness.LOW;
                } else {
                    double heightDiff = (currentHeight - previousHeights[o]) * 4.0 / (double) (i + 4) + ((double) (o + p & 1) - 0.5) * 0.4;
                    if (heightDiff > 0.6) brightness = MapColor.Brightness.HIGH;
                    else if (heightDiff < -0.6) brightness = MapColor.Brightness.LOW;
                }

                previousHeights[o] = currentHeight;
                if (p >= 0) {
                    assert mapColor != null;
                    state.updateColor(o, p, mapColor.getPackedId(brightness));
                }
            }

            if (p >= 0) state.setDirty();

            p++; // Move to next row down
            if (p >= 128) {
                return finish(); // Reached the bottom of the map!
            } else {
                processState = 0; // Loop back to fetch chunks for the next row
                return false;
            }
        }
    }

    private boolean finish() {
        for (ChunkPos pos : activeTickets) {
            world.getChunkSource().removeTicketWithRadius(MAP_BUILDER_TICKET, pos, 1);
        }
        activeTickets.clear();
        return true;
    }

    @Override
    public float getProgress() {
        // Smoothly tracks progress out of 129 total rows (-1 to 127)
        return (p + 1) / 129.0f;
    }
}