package net.hallowed.oldways.mixin.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Shadow public abstract boolean isOf(Block block);

    @Inject(method = "getHardness", at = @At("HEAD"), cancellable = true)
    private void oldways$setHardness(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (isOf(Blocks.NETHERRACK) || isOf(Blocks.CRIMSON_NYLIUM) || isOf(Blocks.WARPED_NYLIUM)) {
            cir.setReturnValue(1.4F);
        }
    }

    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    private void oldways$setLuminance(CallbackInfoReturnable<Integer> cir) {
        if (isOf(Blocks.TORCHFLOWER) || isOf(Blocks.POTTED_TORCHFLOWER)) {
            cir.setReturnValue(5);
        }
        else if (isOf(Blocks.TORCH) || isOf(Blocks.WALL_TORCH) || isOf(Blocks.COPPER_TORCH) || isOf(Blocks.COPPER_WALL_TORCH)) {
            cir.setReturnValue(12);
        }
    }
}
