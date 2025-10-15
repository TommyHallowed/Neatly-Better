package net.hallowed.oldways.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EntityInsideFireHandler {
    private EntityInsideFireHandler() {}

    public static void setLastFireSourceFromBlocks(Entity entity) {
        checkInsideBlocks(entity, (BlockState state) -> setLastFireSourceFromBlock(entity, state));
    }

    public static boolean setLastFireSourceFromBlock(Entity entity, BlockState state) {
        Block b = state.getBlock();
        if (b == Blocks.SOUL_FIRE || b == Blocks.FIRE) {
            FireSourceHolder.of(entity).oldways$setLastFireSource(b);
            return true;
        }
        return false;
    }

    public static boolean isInsideSoulFire(Entity entity) {
        var bb = entity.getBoundingBox();
        int minX = (int)Math.floor(bb.minX + 1.0E-5);
        int minY = (int)Math.floor(bb.minY + 1.0E-5);
        int minZ = (int)Math.floor(bb.minZ + 1.0E-5);
        int maxX = (int)Math.floor(bb.maxX - 1.0E-5);
        int maxY = (int)Math.floor(bb.maxY - 1.0E-5);
        int maxZ = (int)Math.floor(bb.maxZ - 1.0E-5);

        World w = entity.getEntityWorld();
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (w.getBlockState(m.set(x, y, z)).isOf(Blocks.SOUL_FIRE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void checkInsideBlocks(Entity entity, Predicate<BlockState> test) {
        List<BlockPos> positions = new ArrayList<>();
        collectInsideBlocks(entity, positions::add);
        positions.sort(Comparator.comparingDouble(p -> p.toCenterPos().squaredDistanceTo(entity.getEntityPos())));
        World w = entity.getEntityWorld();
        for (BlockPos pos : positions) {
            if (test.test(w.getBlockState(pos))) break;
        }
    }

    private static void collectInsideBlocks(Entity e, Consumer<BlockPos> sink) {
        if (!e.isAlive()) return;
        var bb = e.getBoundingBox();
        int minX = (int)Math.floor(bb.minX + 1.0E-5);
        int minY = (int)Math.floor(bb.minY + 1.0E-5);
        int minZ = (int)Math.floor(bb.minZ + 1.0E-5);
        int maxX = (int)Math.floor(bb.maxX - 1.0E-5);
        int maxY = (int)Math.floor(bb.maxY - 1.0E-5);
        int maxZ = (int)Math.floor(bb.maxZ - 1.0E-5);
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    sink.accept(m.set(x, y, z).toImmutable());
                }
            }
        }
    }
}
