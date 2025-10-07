package net.hallowed.oldways.client.mixin.ui.locatorbar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.locator.WaypointTracking;
import net.hallowed.oldways.client.ui.SmallHudOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWaypointHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    /* ===================== 1) Coords/Time Hud ===================== */
    @Shadow @Final private MinecraftClient client;

    @Unique private static long oldways$lastWaypointNs = 0L;

    @Inject(method = "render", at = @At("TAIL"))
    private void oldways$renderSmallHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ClientConfigManager.overlayEnabled()) return;
        SmallHudOverlay.render(context);
    }
    /* ===================== 2) Locator Bar ===================== */
    @WrapOperation(
            method = "getCurrentBarType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWaypointHandler;hasWaypoint()Z")
    )
    private boolean oldways$injectClientWaypoints(ClientWaypointHandler instance, Operation<Boolean> original) {
        if (!ClientConfigManager.locatorBarEnabled()) return false;

        final boolean showInSpectator = ClientConfigManager.locatorBarShowInSpectator();
        final boolean tabForceEnabled = ClientConfigManager.tabForcesLocatorBar();
        final boolean tabDown = tabForceEnabled && client.options != null && client.options.playerListKey.isPressed();
        if (tabDown) return true;

        final long nowNs = System.nanoTime();
        final long delayNs = Math.max(0L, ClientConfigManager.locatorHideDelayMs()) * 1_000_000L;
        if ((nowNs - oldways$lastWaypointNs) <= delayNs) return true;

        final boolean vanillaHas = original.call(instance);
        if (client.player == null) return vanillaHas;
        if (!showInSpectator && client.player.isSpectator()) return vanillaHas;

        boolean anyClientWp = !WaypointTracking.update(client.player).isEmpty();

        if (!anyClientWp
                && net.hallowed.oldways.client.util.InventoryDeepScan.hasAnyCompass(client.player)
                && !WaypointTracking.WAYPOINTS.isEmpty()) {
            anyClientWp = true;
        }

        if (anyClientWp) oldways$lastWaypointNs = nowNs;
        return vanillaHas || anyClientWp;
    }

    @WrapOperation(
            method = "getCurrentBarType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldShowJumpBar()Z")
    )
    private boolean oldways$suppressJumpWhenForced(InGameHud self, Operation<Boolean> original) {
        final boolean forced =
                ClientConfigManager.locatorBarEnabled()
                        && ClientConfigManager.tabForcesLocatorBar()
                        && client.options != null
                        && client.options.playerListKey.isPressed();
        return !forced && original.call(self);
    }

    @WrapOperation(
            method = "getCurrentBarType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldShowExperienceBar()Z")
    )
    private boolean oldways$suppressXpWhenForced(InGameHud self, Operation<Boolean> original) {
        final boolean forced =
                ClientConfigManager.locatorBarEnabled()
                        && ClientConfigManager.tabForcesLocatorBar()
                        && client.options != null
                        && client.options.playerListKey.isPressed();
        return !forced && original.call(self);
    }
}
