package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.locator.WaypointRendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin implements Bar {
    @Shadow @Final private MinecraftClient client;

    /** Kill the background bar entirely when disabled. */
    @Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
    private void oldways$cancelBarIfDisabled(DrawContext ctx, RenderTickCounter ticks, CallbackInfo ci) {
        if (!ClientConfigManager.locatorBarEnabled()) {
            ci.cancel();
        }
    }

    /** Kill vanilla waypoint dots/arrows when disabled. */
    @Inject(method = "renderAddons", at = @At("HEAD"), cancellable = true)
    private void oldways$cancelAddonsIfDisabled_HEAD(DrawContext ctx, RenderTickCounter ticks, CallbackInfo ci) {
        if (!ClientConfigManager.locatorBarEnabled()) {
            ci.cancel();
        }
    }

    /** When enabled, draw our extra client-side waypoints after vanilla. */
    @Inject(method = "renderAddons", at = @At("RETURN"))
    private void oldways$renderClientWaypoints_RETURN(DrawContext ctx, RenderTickCounter ticks, CallbackInfo ci) {
        if (!ClientConfigManager.locatorBarEnabled()) return;
        WaypointRendering.renderWaypoints(this.client, ctx, this.getCenterY(this.client.getWindow()));
    }
}
