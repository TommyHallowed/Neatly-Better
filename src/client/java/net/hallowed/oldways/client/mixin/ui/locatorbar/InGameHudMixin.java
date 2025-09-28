package net.hallowed.oldways.client.mixin.ui.locatorbar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.locator.WaypointTracking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.world.ClientWaypointHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Unique private static long oldways$lastWaypointMs = 0L;

    /** Make vanilla think there is *no* locator content when the feature is disabled. */
    @WrapOperation(
            method = "getCurrentBarType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWaypointHandler;hasWaypoint()Z")
    )
    private boolean oldways$injectClientWaypoints(ClientWaypointHandler instance, Operation<Boolean> original) {
        // Completely disable the locator bar path.
        if (!ClientConfigManager.locatorBarEnabled()) {
            return false;
        }

        if (client.player == null) {
            return original.call(instance);
        }

        // Our client-side waypoints + small hide-delay window.
        boolean anyClientWp = !WaypointTracking.update(client.player).isEmpty()
                && (ClientConfigManager.locatorBarShowInSpectator() || !client.player.isSpectator());
        boolean tabForce = ClientConfigManager.tabForcesLocatorBar() && client.options.playerListKey.isPressed();

        if (anyClientWp) oldways$lastWaypointMs = System.currentTimeMillis();
        boolean withinDelay = (System.currentTimeMillis() - oldways$lastWaypointMs) <= ClientConfigManager.locatorHideDelayMs();

        // True if vanilla has one, or ours, or forced, or within delay.
        return original.call(instance) || anyClientWp || tabForce || withinDelay;
    }

    /** Do not suppress jump bar unless the feature is enabled & being forced. */
    @WrapOperation(
            method = "getCurrentBarType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldShowJumpBar()Z")
    )
    private boolean oldways$suppressJumpWhenForced(InGameHud self, Operation<Boolean> original) {
        if (ClientConfigManager.locatorBarEnabled()
                && ClientConfigManager.tabForcesLocatorBar()
                && client.options.playerListKey.isPressed()) {
            return false;
        }
        return original.call(self);
    }

    /** Do not suppress XP bar unless the feature is enabled & being forced. */
    @WrapOperation(
            method = "getCurrentBarType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldShowExperienceBar()Z")
    )
    private boolean oldways$suppressXpWhenForced(InGameHud self, Operation<Boolean> original) {
        if (ClientConfigManager.locatorBarEnabled()
                && ClientConfigManager.tabForcesLocatorBar()
                && client.options.playerListKey.isPressed()) {
            return false;
        }
        return original.call(self);
    }
}
