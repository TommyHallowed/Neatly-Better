package net.hallowed.oldways.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.BlockView;
import net.minecraft.util.function.BooleanBiFunction;

public final class FireShapeUtils {
    private FireShapeUtils() {}

    /**
     * Returns true if the block state's outline shape at the given position overlaps the provided entity bounding box.
     * This mirrors the vanilla approach of testing the block's outline (voxel) shape intersection with an entity hitbox.
     */
    public static boolean outlineIntersectsEntity(BlockState state, BlockView world, BlockPos pos, Box entityBox) {
        try {
            VoxelShape outline = state.getOutlineShape(world, pos, ShapeContext.absent());
            if (outline.isEmpty()) return false;

            double relMinX = entityBox.minX - pos.getX();
            double relMinY = entityBox.minY - pos.getY();
            double relMinZ = entityBox.minZ - pos.getZ();
            double relMaxX = entityBox.maxX - pos.getX();
            double relMaxY = entityBox.maxY - pos.getY();
            double relMaxZ = entityBox.maxZ - pos.getZ();

            VoxelShape entityShape = VoxelShapes.cuboid(relMinX, relMinY, relMinZ, relMaxX, relMaxY, relMaxZ);
            return VoxelShapes.matchesAnywhere(outline, entityShape, BooleanBiFunction.AND);
        } catch (Throwable t) {
            return false;
        }
    }
}
