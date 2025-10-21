package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.accessor.EntitySoulFireAccessor;
import net.hallowed.oldways.client.accessor.ClientPlayerEntityAccessor;
import net.hallowed.oldways.init.OldWaysTrackedData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ClientLivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void oldways$clientLivingTick(CallbackInfo ci) {
        try {
            LivingEntity self = (LivingEntity)(Object)this;
            if (self == null || self.getEntityWorld() == null) return;

            // Read server-synced tracked value and update per-entity accessor
            try {
                DataTracker tracker = self.getDataTracker();
                Byte tracked = tracker.get(OldWaysTrackedData.OLDWAYS_SOUL_FIRE);
                boolean isSoul = tracked != null && tracked.byteValue() != 0;
                if (self instanceof EntitySoulFireAccessor acc) {
                    acc.oldways$setSoulFire(isSoul);
                }
                // If this living entity is the local player, also update the client's remembered overlay flag
                try {
                    if (self instanceof ClientPlayerEntity player && player instanceof ClientPlayerEntityAccessor pacc) {
                        pacc.oldways$setSoulFire(isSoul);
                    }
                } catch (Throwable ignored) {
                }
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
    }
}
