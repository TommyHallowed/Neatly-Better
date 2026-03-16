package net.hallowed.neatlybetter.client.mixin.ui.locatorbar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.neatlybetter.client.feature.locator.WaypointRendering;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderPipeline;

@Mixin(LocatorBarRenderer.class)
public abstract class LocatorBarRendererMixin implements ContextualBarRenderer {
    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(
            method = "renderBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
            )
    )
    private void neatlybetter$wrapDrawGuiTexture(
            GuiGraphics ctx,
            RenderPipeline pipeline,
            Identifier id,
            int x, int y, int w, int h,
            Operation<Void> original
    ) {
        boolean isLocatorBackground = id.getPath().contains("locator_bar_background");
        boolean isCreative = minecraft.player != null && minecraft.player.isCreative();

        if (!isLocatorBackground || isCreative) {
            original.call(ctx, pipeline, id, x, y, w, h);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void neatlybetter$renderClientWaypoints_RETURN(GuiGraphics ctx, DeltaTracker ticks, CallbackInfo ci) {
        WaypointRendering.renderWaypoints(this.minecraft, ctx, this.top(this.minecraft.getWindow()));
    }
}