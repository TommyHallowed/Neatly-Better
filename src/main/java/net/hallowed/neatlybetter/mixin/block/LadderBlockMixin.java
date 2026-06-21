package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LadderBlock.class)
public class LadderBlockMixin {

    @Unique
    private static boolean isWallMounted(LevelReader level, BlockPos pos, BlockState ladderState) {
        Direction facing = ladderState.getValue(LadderBlock.FACING);
        BlockPos behind = pos.relative(facing.getOpposite());
        return level.getBlockState(behind).isFaceSturdy(level, behind, facing);
    }

    @Unique
    private static boolean hasSupportInDirection(LevelReader level, BlockPos start, Direction direction) {
        BlockPos pos = start;
        while (true) {
            BlockPos next = pos.relative(direction);
            BlockState nextState = level.getBlockState(next);
            if (!nextState.is(Blocks.LADDER)) {
                return nextState.isFaceSturdy(level, next, direction.getOpposite());
            }
            if (isWallMounted(level, next, nextState)) return true;
            pos = next;
        }
    }

    @Unique
    private static boolean isFreeStanding(LevelReader level, BlockPos start, BlockState startState) {
        return isWallMounted(level, start, startState)
                || hasSupportInDirection(level, start, Direction.DOWN)
                || hasSupportInDirection(level, start, Direction.UP);
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void handleTopBottomPlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> cir) {

        Direction clickedFace = context.getClickedFace();

        if (clickedFace != Direction.UP && clickedFace != Direction.DOWN) {
            return;
        }

        LadderBlock self = (LadderBlock) (Object) this;
        BlockPos pos = context.getClickedPos();

        BlockPos sourcePos = pos.relative(clickedFace.getOpposite());
        BlockState sourceState = context.getLevel().getBlockState(sourcePos);

        Direction facing;
        if (sourceState.is(Blocks.LADDER) && isFreeStanding(context.getLevel(), sourcePos, sourceState)) {
            facing = sourceState.getValue(LadderBlock.FACING);
        } else {
            facing = context.getHorizontalDirection().getOpposite();
        }

        boolean waterlogged = context.getLevel()
                .getFluidState(pos)
                .is(Fluids.WATER);

        cir.setReturnValue(
                self.defaultBlockState()
                        .setValue(LadderBlock.FACING, facing)
                        .setValue(LadderBlock.WATERLOGGED, waterlogged)
        );
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void allowSurvivalOnTopBottom(
            BlockState state,
            LevelReader level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {

        if (isFreeStanding(level, pos, state)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void breakWhenVerticalSupportLost(
            BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos,
            BlockState neighbourState, RandomSource random,
            CallbackInfoReturnable<BlockState> cir) {

        if (directionToNeighbour == Direction.UP || directionToNeighbour == Direction.DOWN) {
            if (!state.canSurvive(level, pos)) {
                cir.setReturnValue(Blocks.AIR.defaultBlockState());
            }
        }
    }
}