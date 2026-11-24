package net.hallowed.neatlybetter.client.mixin.render;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.hallowed.neatlybetter.feature.building.GlowingBanners;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Glowing banners: pattern layers render at full brightness; the base and the pole keep normal lighting. */
@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {

    @Unique private static final RenderStateDataKey<Boolean> NEATLYBETTER$GLOWING = RenderStateDataKey.create(() -> "neatly-better:glowing_banner");

    /**
     * Set while a glowing world banner is submitted. The pattern code is a static helper shared with shields and
     * banner items, so it can't see the render state; submission happens on the render thread only.
     */
    @Unique private static boolean neatlybetter$glowingPatterns;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BannerBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL"))
    private void neatlybetter$extractGlow(BannerBlockEntity banner, BannerRenderState state, float partialTicks, Vec3 cameraPosition,
                                          ModelFeatureRenderer.CrumblingOverlay breakProgress, CallbackInfo ci) {
        state.setData(NEATLYBETTER$GLOWING, GlowingBanners.isGlowing(banner) ? Boolean.TRUE : null);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"))
    private void neatlybetter$beginGlow(BannerRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                        CameraRenderState camera, CallbackInfo ci) {
        neatlybetter$glowingPatterns = Boolean.TRUE.equals(state.getData(NEATLYBETTER$GLOWING));
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("RETURN"))
    private void neatlybetter$endGlow(BannerRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                      CameraRenderState camera, CallbackInfo ci) {
        neatlybetter$glowingPatterns = false;
    }

    /** Light of the pattern layers (ordinal 1: the loop; ordinal 0 is the base colour layer). */
    @ModifyArg(
            method = "submitPatterns",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BannerRenderer;submitPatternLayer(Lnet/minecraft/client/resources/model/sprite/SpriteGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;IILnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/world/item/DyeColor;)V",
                    ordinal = 1),
            index = 3
    )
    private static int neatlybetter$glowPatternLight(int lightCoords) {
        return neatlybetter$glowingPatterns ? LightCoordsUtil.FULL_BRIGHT : lightCoords;
    }
}
