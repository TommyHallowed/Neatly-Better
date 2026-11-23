package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;

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

// Torchflower light level is applied once at startup via BlockStateBaseAccessor (see NeatlyBetter),
// instead of hooking getLightEmission, which the lighting engine calls constantly.
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow public abstract Block getBlock();

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$setHardness(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Block block = getBlock();
        if (block != Blocks.NETHERRACK && block != Blocks.CRIMSON_NYLIUM && block != Blocks.WARPED_NYLIUM) return;
        if (NTServerConfig.CONFIG.harderNetherrack.get()) {
            cir.setReturnValue(1.4F);
        }
    }
}
