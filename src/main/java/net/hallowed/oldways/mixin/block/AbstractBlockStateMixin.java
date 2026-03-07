package net.hallowed.oldways.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin {

    @Shadow public abstract boolean is(Block block);

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void oldways$setHardness(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (is(Blocks.NETHERRACK) || is(Blocks.CRIMSON_NYLIUM) || is(Blocks.WARPED_NYLIUM)) {
            cir.setReturnValue(1.4F);
        }
    }

    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void oldways$setLuminance(CallbackInfoReturnable<Integer> cir) {
        if (is(Blocks.TORCHFLOWER) || is(Blocks.POTTED_TORCHFLOWER)) {
            cir.setReturnValue(5);
        }
    }
}
