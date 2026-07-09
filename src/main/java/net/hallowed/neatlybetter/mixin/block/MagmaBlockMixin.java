package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.util.CopperGrateHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MagmaBlock.class)
public abstract class MagmaBlockMixin extends Block {
    public MagmaBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        neatlybetter$propagateThroughGrates(state, level, pos);
    }

    @Override
    public void destroy(@NonNull LevelAccessor level, @NonNull BlockPos pos, @NonNull BlockState state) {
        neatlybetter$collapseColumnThroughGrates(level, pos);
        super.destroy(level, pos, state);
    }

    @Unique
    private static void neatlybetter$propagateThroughGrates(BlockState sourceState, Level level, BlockPos sourcePos) {
        BlockPos.MutableBlockPos checkPos = sourcePos.mutable().move(Direction.UP);

        while (true) {
            BlockState stateAt = level.getBlockState(checkPos);

            if (CopperGrateHelper.isWaterloggedCopperGrate(stateAt)) {
                while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(checkPos))) {
                    checkPos.move(Direction.UP);
                }
                BubbleColumnBlock.updateColumn(Blocks.BUBBLE_COLUMN, level, checkPos.immutable(), sourceState);
                continue;
            }

            if (stateAt.getBlock() == Blocks.BUBBLE_COLUMN) {
                checkPos.move(Direction.UP);
                continue;
            }
            FluidState fluid = stateAt.getFluidState();
            if (!fluid.isEmpty() && fluid.getType() == Fluids.WATER) {
                checkPos.move(Direction.UP);
                continue;
            }

            break;
        }
    }

    @Unique
    private static void neatlybetter$collapseColumnThroughGrates(LevelAccessor level, BlockPos pos) {
        BlockPos.MutableBlockPos checkPos = pos.mutable().move(Direction.UP);

        if (!CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(checkPos))) {
            return;
        }

        boolean insideGrateStack = true;
        while (true) {
            BlockState current = level.getBlockState(checkPos);

            if (CopperGrateHelper.isWaterloggedCopperGrate(current)) {
                insideGrateStack = true;
                checkPos.move(Direction.UP);
                continue;
            }

            if (insideGrateStack) {
                BubbleColumnBlock.updateColumn(
                        Blocks.BUBBLE_COLUMN, level,
                        checkPos.immutable(),
                        Blocks.AIR.defaultBlockState());
                insideGrateStack = false;
            }

            if (current.isAir()) break;
            if (current.getBlock() == Blocks.BUBBLE_COLUMN) {
                checkPos.move(Direction.UP);
                continue;
            }
            if (!current.getFluidState().isEmpty() && current.getFluidState().getType() == Fluids.WATER) {
                checkPos.move(Direction.UP);
                continue;
            }
            break;
        }
    }
}
