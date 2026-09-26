package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), require = 0)
    private void neatlybetter$igniteOnTouch(BlockState state, Level level, BlockPos pos, Entity entity,
                                            InsideBlockEffectApplier effectApplier, boolean isPrecise, CallbackInfo ci) {
        if (state.getValue(CampfireBlock.LIT) && !entity.fireImmune()) {
            entity.igniteForSeconds(3);
        }
    }
}