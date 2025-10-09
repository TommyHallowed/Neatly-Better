package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.hallowed.oldways.client.feature.locator.WaypointRendering;
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

    @Inject(method = "renderAddons", at = @At("RETURN"))
    private void oldways$renderClientWaypoints_RETURN(DrawContext ctx, RenderTickCounter ticks, CallbackInfo ci) {
        WaypointRendering.renderWaypoints(this.client, ctx, this.getCenterY(this.client.getWindow()));
    }
}
