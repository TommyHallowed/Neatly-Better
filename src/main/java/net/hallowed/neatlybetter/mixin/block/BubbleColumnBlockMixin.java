package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.hallowed.neatlybetter.util.CopperGrateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BubbleColumnBlock.class)
public abstract class BubbleColumnBlockMixin {

    @Inject(
            method = "updateColumn(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void neatlybetter$updateColumnThroughGrates(Block bubbleColumn, LevelAccessor level, BlockPos occupyAt, BlockState occupyState, BlockState belowState, CallbackInfo ci) {
        boolean occupyIsGrate = CopperGrateHelper.isWaterloggedCopperGrate(occupyState);
        boolean belowIsGrate = CopperGrateHelper.isWaterloggedCopperGrate(belowState);
        if (!occupyIsGrate && !belowIsGrate) {
            return;
        }

        BlockPos.MutableBlockPos sourcePos = occupyAt.mutable().move(Direction.DOWN);
        while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(sourcePos))) {
            sourcePos.move(Direction.DOWN);
        }
        BlockState realBelowState = level.getBlockState(sourcePos);

        if (!realBelowState.is(bubbleColumn)
                && !realBelowState.is(BlockTags.ENABLES_BUBBLE_COLUMN_PUSH_UP)
                && !realBelowState.is(BlockTags.ENABLES_BUBBLE_COLUMN_DRAG_DOWN)) {
            ci.cancel();
            return;
        }

        BlockPos.MutableBlockPos topPos = occupyAt.mutable();
        while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(topPos))) {
            topPos.move(Direction.UP);
        }

        BubbleColumnBlock.updateColumn(bubbleColumn, level, topPos.immutable(), realBelowState);
        ci.cancel();
    }

    @ModifyExpressionValue(
            method = "updateShape",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BubbleColumnBlock;canOccupy(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean neatlybetter$updateShapeGrateCheck(boolean original, BlockState state) {
        return original || CopperGrateHelper.isWaterloggedCopperGrate(state);
    }

    @Inject(
            method = "canSurvive",
            at = @At("RETURN"),
            cancellable = true
    )
    private void neatlybetter$canSurviveThroughGrate(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            BlockPos.MutableBlockPos checkPos = pos.mutable().move(Direction.DOWN);
            while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(checkPos))) {
                checkPos.move(Direction.DOWN);
            }
            BlockState realBelow = level.getBlockState(checkPos);
            cir.setReturnValue(
                    realBelow.is((Block) (Object) this)
                            || realBelow.is(BlockTags.ENABLES_BUBBLE_COLUMN_PUSH_UP)
                            || realBelow.is(BlockTags.ENABLES_BUBBLE_COLUMN_DRAG_DOWN)
            );
        }
    }
}