package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.hallowed.oldways.client.feature.locator.WaypointRendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;

@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin implements Bar {
    @Shadow @Final private MinecraftClient client;

    @Redirect(
            method = "renderBar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(" +
                            "Lcom/mojang/blaze3d/pipeline/RenderPipeline;" +
                            "Lnet/minecraft/util/Identifier;" +
                            "IIII)V"
            )
    )
    private void oldways$redirectDrawGuiTexture(
            DrawContext ctx,
            RenderPipeline pipeline,
            Identifier id,
            int x, int y, int w, int h
    ) {
        boolean isLocatorBackground = id.getPath().contains("locator_bar_background");

        boolean isCreative = client.player != null && client.player.isCreative();

        if (!isLocatorBackground || isCreative) {
            ctx.drawGuiTexture(pipeline, id, x, y, w, h);
        }
    }

    @Inject(method = "renderAddons", at = @At("RETURN"))
    private void oldways$renderClientWaypoints_RETURN(DrawContext ctx, RenderTickCounter ticks, CallbackInfo ci) {
        WaypointRendering.renderWaypoints(this.client, ctx, this.getCenterY(this.client.getWindow()));
    }
}
