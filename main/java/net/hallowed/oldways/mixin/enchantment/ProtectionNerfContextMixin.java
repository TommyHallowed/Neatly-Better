package net.hallowed.oldways.mixin.enchantment;

import net.hallowed.oldways.enchantment.ProtectionContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Provide context exactly where vanilla computes enchantment protection and
 * calls DamageUtil#getInflictedDamage.
 */
@Mixin(LivingEntity.class)
public abstract class ProtectionNerfContextMixin {

    @Inject(method = "modifyAppliedDamage", at = @At("HEAD"))
    private void oldways$setProtContext(DamageSource source, float amount,
                                        CallbackInfoReturnable<Float> cir) {
        ProtectionContext.set((LivingEntity)(Object)this, source);
    }

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"))
    private void oldways$clearProtContext(DamageSource source, float amount,
                                          CallbackInfoReturnable<Float> cir) {
        ProtectionContext.clear();
    }
}
