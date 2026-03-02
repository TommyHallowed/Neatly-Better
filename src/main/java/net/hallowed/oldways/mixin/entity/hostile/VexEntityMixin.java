package net.hallowed.oldways.mixin.entity.hostile;

import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.VexEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("deprecation")
@Mixin(VexEntity.class)
public abstract class VexEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void oldways$dieWhenEvokerOwnerDies(CallbackInfo ci) {
        VexEntity self = (VexEntity)(Object)this;
        if (self.getEntityWorld().isClient()) return;

        MobEntity owner = self.getOwner();
        if (owner instanceof EvokerEntity && !owner.isAlive()) {
            self.serverDamage(self.getDamageSources().magic(), Float.MAX_VALUE);
        }
    }
}
