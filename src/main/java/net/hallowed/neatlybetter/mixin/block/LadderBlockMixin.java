package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
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
    private static boolean isFreeStanding(LevelReader level, BlockPos pos) {

        boolean solidBelow = level.getBlockState(pos.below())
                .isFaceSturdy(level, pos.below(), Direction.UP);
        boolean solidAbove = level.getBlockState(pos.above())
                .isFaceSturdy(level, pos.above(), Direction.DOWN);

        if (solidBelow || solidAbove) return true;

        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.LADDER) && isFreeStanding(level, pos.below())) return true;

        BlockState above = level.getBlockState(pos.above());
        return above.is(Blocks.LADDER) && isFreeStanding(level, pos.above());
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
        if (sourceState.is(Blocks.LADDER) && isFreeStanding(context.getLevel(), sourcePos)) {
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

        if (isFreeStanding(level, pos)) {
            cir.setReturnValue(true);
        }
    }
}