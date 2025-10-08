package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.feature.stuckprojectile.StuckProjectileFeature;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Attaches the stuck-projectile features to ALL living renderers, when enabled. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Shadow protected abstract boolean addFeature(FeatureRenderer<S, M> feature);

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<init>", at = @At("TAIL"))
    private void oldways$addFeatures(EntityRendererFactory.Context bakeCtx, M model, float shadowRadius, CallbackInfo ci) {
        if (!ClientConfigManager.stuckProjectilesEnabled()) return;

        FeatureRendererContext<S, M> parent = (FeatureRendererContext<S, M>) this;
        addFeature(new StuckProjectileFeature.Arrows(parent, bakeCtx));
        addFeature(new StuckProjectileFeature.Stingers(parent, bakeCtx));
    }
}
