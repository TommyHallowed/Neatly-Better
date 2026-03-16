package net.hallowed.neatlybetter.client.mixin.render.banner;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.hallowed.neatlybetter.api.EmissiveBannerAccessor;
import net.hallowed.neatlybetter.client.render.EmissiveBannerState;
import net.hallowed.neatlybetter.client.render.ModRenderTypes;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {

    @Inject(method = "extractRenderState*", at = @At("TAIL"))
    private void neatlybetter$extractEmissive(BannerBlockEntity banner,
                                              BannerRenderState state,
                                              float partialTick, Vec3 cameraPos,
                                              ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling,
                                              CallbackInfo ci) {
        ((EmissiveBannerAccessor) state).neatlybetter$setEmissive(
                ((EmissiveBannerAccessor) banner).neatlybetter$isEmissive()
        );
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"))
    private void neatlybetter$setEmissiveBefore(BannerRenderState state,
                                                PoseStack poseStack,
                                                SubmitNodeCollector collector,
                                                CameraRenderState camera,
                                                CallbackInfo ci) {
        EmissiveBannerState.setEmissive(
                ((EmissiveBannerAccessor) state).neatlybetter$isEmissive()
        );
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("RETURN"))
    private void neatlybetter$clearEmissive(BannerRenderState state,
                                            PoseStack poseStack,
                                            SubmitNodeCollector collector,
                                            CameraRenderState camera,
                                            CallbackInfo ci) {
        EmissiveBannerState.setEmissive(false);
    }

    @Redirect(method = "submitPatternLayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/Material;renderType(Ljava/util/function/Function;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType neatlybetter$emissiveRenderType(Material material,
                                                              Function<Identifier, RenderType> original) {
        if (EmissiveBannerState.isEmissive()
                && material != Sheets.BANNER_BASE
                && material != Sheets.SHIELD_BASE) {
            return material.renderType(ModRenderTypes::getEmissiveBanner);
        }
        return material.renderType(original);
    }
}
