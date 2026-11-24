package net.hallowed.neatlybetter.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Bubble columns pass through stacks of <b>waterlogged</b> copper grates. Dry grates block them.
 * Every copper grate (incl. weathering/waxed variants) is a {@link WaterloggedTransparentBlock}.
 */
public final class CopperGrateHelper {
    private CopperGrateHelper() {}

    /** Bubble column water tick delay, same as vanilla LiquidBlock / BubbleColumnBlock. */
    private static final int WATER_COLUMN_DELAY = 20;
    private static final int BUBBLE_COLUMN_DELAY = 5;

    public static boolean isWaterloggedCopperGrate(BlockState state) {
        return state.getBlock() instanceof WaterloggedTransparentBlock
                && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    /** True if {@code state} can feed a bubble column above it (a column, magma or soul sand). */
    public static boolean feedsColumn(Block bubbleColumn, BlockState state) {
        return state.is(bubbleColumn)
                || state.is(BlockTags.ENABLES_BUBBLE_COLUMN_PUSH_UP)
                || state.is(BlockTags.ENABLES_BUBBLE_COLUMN_DRAG_DOWN);
    }

    /** True for blocks a bubble column can occupy (mirrors vanilla BubbleColumnBlock.canOccupy). */
    public static boolean canHoldColumn(Block bubbleColumn, BlockState state) {
        return state.is(bubbleColumn)
                || (state.getBlock() instanceof LiquidBlock && state.getFluidState().isSource() && state.getFluidState().isFull());
    }

    /**
     * Schedules a re-check of the bubble column directly above the waterlogged grate stack that starts
     * at {@code grateBelowPos.above()}. Used when something under the stack changed: grates don't change
     * state themselves, so without this the column above the stack would never notice.
     */
    public static void scheduleColumnAboveStack(LevelReader level, ScheduledTickAccess ticks, BlockPos stackStart) {
        BlockPos.MutableBlockPos pos = stackStart.mutable();
        while (isWaterloggedCopperGrate(level.getBlockState(pos))) {
            pos.move(Direction.UP);
        }
        BlockState above = level.getBlockState(pos);
        if (above.is(Blocks.BUBBLE_COLUMN)) {
            ticks.scheduleTick(pos.immutable(), Blocks.BUBBLE_COLUMN, BUBBLE_COLUMN_DELAY);
        } else if (above.getBlock() instanceof LiquidBlock && above.getFluidState().isSource()) {
            ticks.scheduleTick(pos.immutable(), above.getBlock(), WATER_COLUMN_DELAY);
        }
    }
}
