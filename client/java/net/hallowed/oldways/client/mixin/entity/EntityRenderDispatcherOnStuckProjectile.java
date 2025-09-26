package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.stuckprojectile.StuckProjectilesState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Copies the live counts from the entity so our feature can render them for any mob. */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherOnStuckProjectile {
    // 1.21.8: render(Entity, DDD, F, MatrixStack, VertexConsumerProvider, int)
    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void oldways$captureCounts(Entity e, double x, double y, double z, float tickProgress,
                                       MatrixStack matrices, VertexConsumerProvider providers, int light,
                                       CallbackInfo ci) {
        if (!ClientConfigManager.stuckProjectilesEnabled()) {
            StuckProjectilesState.ENTITY_ID = 0;
            StuckProjectilesState.ARROW_COUNT = 0;
            StuckProjectilesState.STINGER_COUNT = 0;
            return;
        }

        StuckProjectilesState.ENTITY_ID = e.getId();
        if (e instanceof LivingEntity living) {
            StuckProjectilesState.ARROW_COUNT   = living.getStuckArrowCount();
            StuckProjectilesState.STINGER_COUNT = living.getStingerCount();
        } else {
            StuckProjectilesState.ARROW_COUNT = 0;
            StuckProjectilesState.STINGER_COUNT = 0;
        }
    }
}
