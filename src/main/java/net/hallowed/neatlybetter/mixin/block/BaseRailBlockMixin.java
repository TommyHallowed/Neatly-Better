package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockMixin {
    /** Max rails between a floating rail and its nearest anchor. */
    @Unique private static final int neatlybetter$MAX_CHAIN = 16;
    @Unique private static final Direction[] neatlybetter$HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    // Placement + detector rails: a rail may exist without ground if it is chained to an anchor.
    @ModifyReturnValue(method = "canSurvive", at = @At("RETURN"))
    private boolean neatlybetter$allowFloating(boolean original, BlockState state, LevelReader level, BlockPos pos) {
        return original || neatlybetter$isSupported(level, pos);
    }

    // Block updates: vanilla only gets to remove the rail if it is not anchored either.
    // Also means ramps no longer pop when the uphill block is gone, as long as the rail is anchored.
    @ModifyReturnValue(method = "shouldBeRemoved", at = @At("RETURN"))
    private static boolean neatlybetter$keepFloating(boolean original, BlockPos pos, Level level, RailShape shape) {
        return original && !neatlybetter$isSupported(level, pos);
    }

    // Vanilla only notifies the 6 face neighbors on removal. Ramp links are diagonal, so wake those
    // rails ourselves; otherwise a rail hanging off a removed anchor would never re-check its support.
    @Inject(method = "affectNeighborsAfterRemoval", at = @At("TAIL"))
    private void neatlybetter$wakeLinkedRails(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston, CallbackInfo ci) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : neatlybetter$HORIZONTAL) {
            for (int dy = -1; dy <= 1; dy += 2) {
                cursor.set(pos.getX() + dir.getStepX(), pos.getY() + dy, pos.getZ() + dir.getStepZ());
                if (!level.hasChunkAt(cursor)) continue;
                BlockState neighbour = level.getBlockState(cursor);
                if (BaseRailBlock.isRail(neighbour)) {
                    level.neighborChanged(neighbour, cursor.immutable(), state.getBlock(), null, false);
                }
            }
        }
    }

    /** Directly held up: solid ground below, or a sturdy wall on any horizontal side. */
    @Unique
    private static boolean neatlybetter$isAnchor(LevelReader level, BlockPos pos, BlockPos.MutableBlockPos scratch) {
        scratch.setWithOffset(pos, Direction.DOWN);
        if (level.getBlockState(scratch).isFaceSturdy(level, scratch, Direction.UP, SupportType.RIGID)) return true;

        for (Direction dir : neatlybetter$HORIZONTAL) {
            scratch.setWithOffset(pos, dir);
            if (level.hasChunkAt(scratch) && level.getBlockState(scratch).isFaceSturdy(level, scratch, dir.getOpposite())) return true;
        }
        return false;
    }

    /**
     * Anchored itself, or connected through adjacent rails (same level, one up or one down) to an
     * anchored rail within {@link #neatlybetter$MAX_CHAIN}. Breadth-first with early exit, so rails
     * that are already anchored (all vanilla ones) never reach the allocation below.
     */
    @Unique
    private static boolean neatlybetter$isSupported(LevelReader level, BlockPos origin) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        if (neatlybetter$isAnchor(level, origin, scratch)) return true;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        LongOpenHashSet visited = new LongOpenHashSet();
        LongArrayList frontier = new LongArrayList();
        LongArrayList next = new LongArrayList();
        visited.add(origin.asLong());
        frontier.add(origin.asLong());

        for (int depth = 0; depth < neatlybetter$MAX_CHAIN && !frontier.isEmpty(); depth++) {
            for (int i = 0; i < frontier.size(); i++) {
                long packed = frontier.getLong(i);
                int x = BlockPos.getX(packed);
                int y = BlockPos.getY(packed);
                int z = BlockPos.getZ(packed);

                for (Direction dir : neatlybetter$HORIZONTAL) {
                    for (int dy = -1; dy <= 1; dy++) {
                        cursor.set(x + dir.getStepX(), y + dy, z + dir.getStepZ());
                        // Never force-load chunks from a block update; unknown territory counts as supported.
                        if (!level.hasChunkAt(cursor)) return true;
                        if (!visited.add(cursor.asLong())) continue;
                        if (!BaseRailBlock.isRail(level.getBlockState(cursor))) continue;
                        if (neatlybetter$isAnchor(level, cursor, scratch)) return true;
                        next.add(cursor.asLong());
                    }
                }
            }
            LongArrayList swap = frontier;
            frontier = next;
            next = swap;
            next.clear();
        }
        return false;
    }
}