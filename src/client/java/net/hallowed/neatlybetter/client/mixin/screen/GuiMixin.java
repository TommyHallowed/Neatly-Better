package net.hallowed.neatlybetter.client.mixin.screen;

import com.google.common.collect.Ordering;

import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.client.feature.ui.SmallHudOverlay;
import net.hallowed.neatlybetter.client.render.EffectBarRenderer;
import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @Inject(method = "extractEffects", at = @At("TAIL"))
    private void neatlybetter$renderHUDEffectBars(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.effectBars.get()) return;

        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty()) return;

        int beneficialCount = 0;
        int harmfulCount = 0;

        for (MobEffectInstance effect : Ordering.natural().reverse().sortedCopy(collection)) {
            if (!effect.showIcon()) continue;

            boolean isBeneficial = effect.getEffect().value().isBeneficial();
            int x = graphics.guiWidth();
            int y = 1;

            if (this.minecraft.isDemo()) {
                y += 15;
            }

            if (isBeneficial) {
                beneficialCount++;
                x -= 25 * beneficialCount;
            } else {
                harmfulCount++;
                x -= 25 * harmfulCount;
                y += 26;
            }

            EffectBarRenderer.renderHUD(graphics, effect, x, y);
        }
    }

    @Inject(method = "extractAirBubbles", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$hideAirBubblesWithWaterBreathing(
            GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir, int xRight, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.hideAirBubbles.get()) return;
        if (player.hasEffect(MobEffects.WATER_BREATHING)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void neatlybetter$renderSmallHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if ((!NTClientConfig.CONFIG.showCoords.get() && !NTClientConfig.CONFIG.showTime.get()) || this.minecraft.options.hideGui) return;
        SmallHudOverlay.render(graphics);
    }

    @Inject(method = "willPrioritizeExperienceInfo", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$alwaysShowXp(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "willPrioritizeJumpInfo", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$alwaysShowJump(CallbackInfoReturnable<Boolean> cir) {
        if (NTCompat.HORSEMAN) return;
        cir.setReturnValue(true);
    }

    @Shadow
    private void extractFood(GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight) {}

    @Inject(method = "extractPlayerHealth", at = @At("TAIL"))
    private void neatlybetter$alwaysRenderFood(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (NTCompat.HORSEMAN) return;
        Player player = minecraft.player;
        if (player == null) return;

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity mount && mount.isAlive()) {
            int xRight    = graphics.guiWidth()  / 2 + 91;
            int yLineBase = graphics.guiHeight() - 39;
            this.extractFood(graphics, player, yLineBase, xRight);
        }
    }

    @Inject(method = "extractVehicleHealth", at = @At("HEAD"))
    private void neatlybetter$moveHorseHeartsUp(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (NTCompat.HORSEMAN) return;
        var client = Minecraft.getInstance();
        if (client.player == null || client.player.getAbilities().instabuild) {
            return;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(0, -10);
    }

    @Inject(method = "extractVehicleHealth", at = @At("RETURN"))
    private void neatlybetter$restoreMatrix(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (NTCompat.HORSEMAN) return;
        if (minecraft.player != null && !minecraft.player.getAbilities().instabuild) {
            graphics.pose().popMatrix();
        }
    }
}
