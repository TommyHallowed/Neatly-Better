package net.hallowed.oldways.client.mixin.ui.locatorbar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.hallowed.oldways.client.feature.locator.WaypointTracking;
import net.hallowed.oldways.client.feature.ui.SmallHudOverlay;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWaypointHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final public MinecraftClient client;

    @Unique private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    /* ===================== 1) Small HUD Overlay ===================== */
    @Inject(method = "render", at = @At("TAIL"))
    private void oldways$renderSmallHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if ((!OW$prefs.showCoords && !OW$prefs.showTime) || this.client.options.hudHidden) return;
        SmallHudOverlay.render(context);
    }

    /* ===================== 2) Locator Bar ===================== */
    @WrapOperation(
            method = "getCurrentBarType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWaypointHandler;hasWaypoint()Z")
    )
    private boolean oldways$injectClientWaypoints(ClientWaypointHandler instance, Operation<Boolean> original) {
        final boolean vanillaHas = original.call(instance);
        if (client.player == null) return vanillaHas;

        boolean anyClientWp = !WaypointTracking.update(client.player).isEmpty();
        if (!anyClientWp
                && InventoryDeepScan.hasAnyCompass(client.player)
                && !WaypointTracking.WAYPOINTS.isEmpty()) {
            anyClientWp = true;
        }
        return vanillaHas || anyClientWp;
    }

    /* ===================== 3) Always show XP & Jump Bars ===================== */
    @Inject(method = "shouldShowExperienceBar", at = @At("RETURN"), cancellable = true)
    private void oldways$alwaysShowXp(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "shouldShowJumpBar", at = @At("RETURN"), cancellable = true)
    private void oldways$alwaysShowJump(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    /* ===================== 4) Show Hunger Bar while on Horse ===================== */
    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void oldways$alwaysRenderFood(DrawContext context, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (player == null) return;

        LivingEntity mount = (LivingEntity) player.getVehicle();
        if (mount != null && mount.isAlive()) {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            ((InGameHud)(Object)this).renderFood(context, player, screenHeight - 39, screenWidth / 2 + 91);
        }
    }

    /* ===================== 5) Move Horse Health Bar up slightly ===================== */
    @Inject(method = "renderMountHealth", at = @At("HEAD"))
    private void oldways$moveHorseHeartsUp(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, -10);
    }

    @Inject(method = "renderMountHealth", at = @At("RETURN"))
    private void oldways$restoreMatrix(DrawContext context, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }
}
