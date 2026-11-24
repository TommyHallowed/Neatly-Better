package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.hallowed.neatlybetter.util.CopperGrateHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bubble columns pass through stacks of waterlogged copper grates.
 * Updates are driven the vanilla way (scheduled ticks), see also LiquidBlockMixin and CopperGrateBlockMixin.
 */
@Mixin(BubbleColumnBlock.class)
public abstract class BubbleColumnBlockMixin {

    @Unique
    private static final String UPDATE_COLUMN =
            "updateColumn(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V";

    /**
     * Called with a grate as the occupied block or as the block below (e.g. a tick on the water above a
     * grate stack). Resolve the real source under the stack and rebuild, or clear, the column above it.
     */
    @Inject(method = UPDATE_COLUMN, at = @At("HEAD"), cancellable = true)
    private static void neatlybetter$updateColumnThroughGrates(Block bubbleColumn, LevelAccessor level, BlockPos occupyAt,
                                                               BlockState occupyState, BlockState belowState, CallbackInfo ci) {
        if (!CopperGrateHelper.isWaterloggedCopperGrate(occupyState)
                && !CopperGrateHelper.isWaterloggedCopperGrate(belowState)) {
            return;
        }

        BlockPos.MutableBlockPos sourcePos = occupyAt.mutable().move(Direction.DOWN);
        while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(sourcePos))) {
            sourcePos.move(Direction.DOWN);
        }
        BlockState realBelow = level.getBlockState(sourcePos);

        BlockPos.MutableBlockPos topPos = occupyAt.mutable();
        while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(topPos))) {
            topPos.move(Direction.UP);
        }

        // No source under the grates: pass air so vanilla turns any leftover column back into water.
        BlockState effectiveBelow = CopperGrateHelper.feedsColumn(bubbleColumn, realBelow)
                ? realBelow
                : Blocks.AIR.defaultBlockState();
        BubbleColumnBlock.updateColumn(bubbleColumn, level, topPos.immutable(), effectiveBelow);
        ci.cancel();
    }

    /**
     * Vanilla fills (or clears) a column upward until it hits a block it can't occupy, using flag 2,
     * so a grate stack in the way is never notified. Continue the same change above the stack.
     */
    @Inject(method = UPDATE_COLUMN, at = @At("RETURN"))
    private static void neatlybetter$continueAboveGrates(Block bubbleColumn, LevelAccessor level, BlockPos occupyAt,
                                                         BlockState occupyState, BlockState belowState, CallbackInfo ci) {
        if (!CopperGrateHelper.canHoldColumn(bubbleColumn, occupyState)) return;

        BlockPos.MutableBlockPos pos = occupyAt.mutable();
        while (CopperGrateHelper.canHoldColumn(bubbleColumn, level.getBlockState(pos))) {
            pos.move(Direction.UP);
        }
        if (!CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(pos))) return;

        BlockState underStack = level.getBlockState(pos.below());
        while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(pos))) {
            pos.move(Direction.UP);
        }
        BubbleColumnBlock.updateColumn(bubbleColumn, level, pos.immutable(), underStack);
    }

    @ModifyExpressionValue(
            method = "updateShape",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BubbleColumnBlock;canOccupy(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean neatlybetter$updateShapeGrateCheck(boolean original, BlockState state) {
        return original || CopperGrateHelper.isWaterloggedCopperGrate(state);
    }

    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$canSurviveThroughGrate(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;

        BlockPos.MutableBlockPos checkPos = pos.mutable().move(Direction.DOWN);
        if (!CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(checkPos))) return;
        while (CopperGrateHelper.isWaterloggedCopperGrate(level.getBlockState(checkPos))) {
            checkPos.move(Direction.DOWN);
        }
        cir.setReturnValue(CopperGrateHelper.feedsColumn((Block) (Object) this, level.getBlockState(checkPos)));
    }
}
