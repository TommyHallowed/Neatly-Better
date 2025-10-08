package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.feature.stuckprojectile.StuckProjectilesState;
import net.hallowed.oldways.client.util.FlameOverlayState;
import net.hallowed.oldways.util.FireSourceHolder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    /* ===================== 1) Stuck Arrows/Stingers ===================== */

    @Unique
    private static final ThreadLocal<Entity> OLDWAYS$current = new ThreadLocal<>();

    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V",
            at = @At("HEAD")
    )
    private <E extends Entity, S extends EntityRenderState> void oldways$captureCounts(
            E e, double x, double y, double z, float tickProgress,
            MatrixStack matrices, VertexConsumerProvider providers, int light,
            EntityRenderer<? super E, S> renderer, CallbackInfo ci
    ) {
        OLDWAYS$current.set(e);

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

    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V",
            at = @At("RETURN")
    )
    private <E extends Entity, S extends EntityRenderState> void oldways$clearCurrent(
            E e, double x, double y, double z, float tickProgress,
            MatrixStack matrices, VertexConsumerProvider providers, int light,
            EntityRenderer<? super E, S> renderer, CallbackInfo ci
    ) {
        OLDWAYS$current.remove();
    }

    /* ===================== 2) Fire swap for soul-fire ===================== */

    @ModifyVariable(
            method = "renderFire(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private Sprite oldways$swapFire0(Sprite sprite) {
        Entity e = OLDWAYS$current.get();
        if (e instanceof FireSourceHolder) {
            return FlameOverlayState.sprite0(e);
        }
        return sprite;
    }

    @ModifyVariable(
            method = "renderFire(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V",
            at = @At("STORE"),
            ordinal = 1
    )
    private Sprite oldways$swapFire1(Sprite sprite) {
        Entity e = OLDWAYS$current.get();
        if (e instanceof FireSourceHolder) {
            return FlameOverlayState.sprite1(e);
        }
        return sprite;
    }
}
