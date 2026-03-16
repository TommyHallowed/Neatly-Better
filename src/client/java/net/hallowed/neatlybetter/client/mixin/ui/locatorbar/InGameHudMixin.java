package net.hallowed.neatlybetter.client.mixin.ui.locatorbar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.client.feature.locator.WaypointTracking;
import net.hallowed.neatlybetter.client.feature.ui.SmallHudOverlay;
import net.hallowed.neatlybetter.client.util.BackpackCheckClient;
import net.hallowed.neatlybetter.client.util.InventoryDeepScan;
import net.hallowed.neatlybetter.client.util.SettingsPrefs;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Shadow @Final
    private Minecraft minecraft;

    @Unique private static final SettingsPrefs neatlybetter$prefs = SettingsPrefs.get();

    /* ===================== 1) Small HUD Overlay ===================== */
    @Inject(method = "render", at = @At("TAIL"))
    private void neatlybetter$renderSmallHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if ((!neatlybetter$prefs.showCoords && !neatlybetter$prefs.showTime) || this.minecraft.options.hideGui) return;
        SmallHudOverlay.render(context);
    }

    /* ===================== 2) Locator Bar ===================== */
    @WrapOperation(
            method = "nextContextualInfoState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z")
    )
    private boolean neatlybetter$injectClientWaypoints(ClientWaypointManager instance, Operation<Boolean> original) {
        final boolean vanillaHas = original.call(instance);
        if (minecraft.player == null) return vanillaHas;

        boolean anyClientWp = !WaypointTracking.update(minecraft.player).isEmpty();
        if (!anyClientWp
                && (InventoryDeepScan.hasAnyCompass(minecraft.player) || BackpackCheckClient.backpackHasAnyCompass())
                && !WaypointTracking.WAYPOINTS.isEmpty()) {
            anyClientWp = true;
        }
        return vanillaHas || anyClientWp;
    }

    /* ===================== 3) Always show XP & Jump Bars ===================== */
    @Inject(method = "willPrioritizeExperienceInfo", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$alwaysShowXp(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "willPrioritizeJumpInfo", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$alwaysShowJump(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    /* ===================== 4) Show Hunger Bar while on Horse ===================== */
    @Inject(method = "renderPlayerHealth", at = @At("TAIL"))
    private void neatlybetter$alwaysRenderFood(GuiGraphics context, CallbackInfo ci) {
        if (NTCompat.HORSEMAN) return;
        Player player = minecraft.player;
        if (player == null) return;

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity mount && mount.isAlive()) {
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();

            ((Gui)(Object)this).renderFood(context, player, screenHeight - 39, screenWidth / 2 + 91);
        }
    }


    /* ===================== 5) Move Horse Health Bar up slightly ===================== */
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"))
    private void neatlybetter$moveHorseHeartsUp(GuiGraphics context, CallbackInfo ci) {
        if (NTCompat.HORSEMAN) return;
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.player == null || client.player.getAbilities().instabuild) {
            return;
        }

        context.pose().pushMatrix();
        context.pose().translate(0, -10);
    }

    @Inject(method = "renderVehicleHealth", at = @At("RETURN"))
    private void neatlybetter$restoreMatrix(GuiGraphics context, CallbackInfo ci) {
        if (NTCompat.HORSEMAN) return;
        if (minecraft.player != null && !minecraft.player.getAbilities().instabuild) {
            context.pose().popMatrix();
        }
    }
}
