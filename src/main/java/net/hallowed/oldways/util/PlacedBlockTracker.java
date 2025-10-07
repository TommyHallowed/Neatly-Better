package net.hallowed.oldways.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class PlacedBlockTracker {
    public static void markPlaced(ServerWorld world, BlockPos pos) { PlacedBlocksState.get(world).add(pos.asLong()); }
    public static boolean wasPlaced(ServerWorld world, BlockPos pos) { return PlacedBlocksState.get(world).has(pos.asLong()); }
    public static void unmark(ServerWorld world, BlockPos pos) { PlacedBlocksState.get(world).remove(pos.asLong()); }
}
