package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow public abstract Block getBlock();

    @Unique
    Block block = getBlock();

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$setHardness(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (!NTServerConfig.CONFIG.harderNetherrack.get()) return;
        if (block == Blocks.NETHERRACK || block == Blocks.CRIMSON_NYLIUM || block == Blocks.WARPED_NYLIUM) {
            cir.setReturnValue(1.4F);
        }
    }

    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$setLuminance(CallbackInfoReturnable<Integer> cir) {
        if (block == Blocks.TORCHFLOWER || block == Blocks.POTTED_TORCHFLOWER) {
            cir.setReturnValue(5);
        }
    }
}
