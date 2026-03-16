package net.hallowed.neatlybetter.client.mixin.entity;

import net.hallowed.neatlybetter.util.NTEffectInstance;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ClientEffectSyncMixin {

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
    private void neatlybetter$preserveMaxDurationClient(MobEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LocalPlayer self) {
            MobEffectInstance oldEffect = self.getEffect(effect.getEffect());

            if (oldEffect != null && oldEffect.getAmplifier() == effect.getAmplifier()) {
                NTEffectInstance newDuck = (NTEffectInstance) effect;
                NTEffectInstance oldDuck = (NTEffectInstance) oldEffect;

                if (newDuck.neatlybetter$getMaxDuration() < oldDuck.neatlybetter$getMaxDuration()) {
                    newDuck.neatlybetter$setMaxDuration(oldDuck.neatlybetter$getMaxDuration());
                }
            }
        }
    }

    @Inject(method = "forceAddEffect", at = @At("HEAD"))
    private void neatlybetter$preserveMaxDurationClientSync(MobEffectInstance effect, @Nullable Entity source, CallbackInfo ci) {
        if ((Object) this instanceof LocalPlayer self) {
            MobEffectInstance oldEffect = self.getEffect(effect.getEffect());

            if (oldEffect != null && oldEffect.getAmplifier() == effect.getAmplifier()) {
                NTEffectInstance newDuck = (NTEffectInstance) effect;
                NTEffectInstance oldDuck = (NTEffectInstance) oldEffect;

                if (newDuck.neatlybetter$getMaxDuration() < oldDuck.neatlybetter$getMaxDuration()) {
                    newDuck.neatlybetter$setMaxDuration(oldDuck.neatlybetter$getMaxDuration());
                }
            }
        }
    }
}