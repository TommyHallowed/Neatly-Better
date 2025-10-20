package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.init.OldWaysTrackedData;
import net.hallowed.oldways.client.accessor.EntityRenderStateAccessor;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void oldways$copyTrackedSoulFire(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        try {
            DataTracker tracker = entity.getDataTracker();
            Byte tracked = tracker.get(OldWaysTrackedData.OLDWAYS_SOUL_FIRE);
            boolean isSoul = tracked != null && tracked.byteValue() != 0;
            if (state instanceof EntityRenderStateAccessor ers) {
                ers.oldways$setSoulFire(isSoul);
            }
        } catch (Throwable ignored) {
        }
    }
}
