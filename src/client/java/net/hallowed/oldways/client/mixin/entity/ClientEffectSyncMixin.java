package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.util.OWEffectInstance;
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
    private void oldways$preserveMaxDurationClient(MobEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LocalPlayer self) {
            MobEffectInstance oldEffect = self.getEffect(effect.getEffect());

            if (oldEffect != null && oldEffect.getAmplifier() == effect.getAmplifier()) {
                OWEffectInstance newDuck = (OWEffectInstance) effect;
                OWEffectInstance oldDuck = (OWEffectInstance) oldEffect;

                if (newDuck.oldways$getMaxDuration() < oldDuck.oldways$getMaxDuration()) {
                    newDuck.oldways$setMaxDuration(oldDuck.oldways$getMaxDuration());
                }
            }
        }
    }

    @Inject(method = "forceAddEffect", at = @At("HEAD"))
    private void oldways$preserveMaxDurationClientSync(MobEffectInstance effect, @Nullable Entity source, CallbackInfo ci) {
        if ((Object) this instanceof LocalPlayer self) {
            MobEffectInstance oldEffect = self.getEffect(effect.getEffect());

            if (oldEffect != null && oldEffect.getAmplifier() == effect.getAmplifier()) {
                OWEffectInstance newDuck = (OWEffectInstance) effect;
                OWEffectInstance oldDuck = (OWEffectInstance) oldEffect;

                if (newDuck.oldways$getMaxDuration() < oldDuck.oldways$getMaxDuration()) {
                    newDuck.oldways$setMaxDuration(oldDuck.oldways$getMaxDuration());
                }
            }
        }
    }
}