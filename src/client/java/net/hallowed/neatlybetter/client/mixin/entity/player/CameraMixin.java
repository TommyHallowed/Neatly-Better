package net.hallowed.neatlybetter.client.mixin.entity.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.hallowed.neatlybetter.client.content.feature.Zoom;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Entity entity;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;
    @Shadow private boolean isPanoramicMode;

    @Inject(method = "tick", at = @At("TAIL"))
    private void neatlybetter$applyInstantSneakEyeHeight(CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.instantEyeHeight.get()) {
            return;
        }

        if (this.entity != null) {
            this.eyeHeightOld = this.eyeHeight = this.entity.getEyeHeight();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void neatlybetter$tickZoom(CallbackInfo ci) {
        Zoom.tick();
    }

    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float neatlybetter$applyZoomFov(float original, @Local(argsOnly = true, name = "partialTicks") float partialTicks) {
        return this.isPanoramicMode ? original : original * Zoom.fovMultiplier(partialTicks);
    }

    @ModifyReturnValue(method = "calculateHudFov", at = @At("RETURN"))
    private float neatlybetter$hideHandWhileZoomed(float original, @Local(argsOnly = true, name = "partialTicks") float partialTicks) {
        if (!NTClientConfig.CONFIG.zoomHideHand.get()) {
            return original;
        }

        float progress = Zoom.progress(partialTicks);
        return progress > 0.0F ? Mth.lerp(progress, original, 1.0F) : original;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void neatlybetter$reduceZoomBobbing(CameraRenderState cameraState, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!cameraState.entityRenderState.isPlayer) {
            return;
        }

        float progress = Zoom.progress(cameraState.cameraEntityPartialTicks);
        if (progress > 0.0F) {
            float reduction = NTClientConfig.CONFIG.zoomBobbingReduction.get().floatValue();
            cameraState.entityRenderState.bob *= 1.0F - reduction * progress;
        }
    }
}