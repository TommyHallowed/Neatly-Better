package net.hallowed.neatlybetter.client.mixin.screen;

import com.google.common.collect.Ordering;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.client.render.EffectBarRenderer;
import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.hallowed.neatlybetter.client.util.FloatBlitSprite;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @Inject(method = "extractEffects", at = @At("TAIL"))
    private void neatlybetter$renderHUDEffectBars(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.effectBars.get()) return;

        assert this.minecraft.player != null;
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
        if ((player.hasEffect(MobEffects.WATER_BREATHING) || (player.hasEffect(MobEffects.BREATH_OF_THE_NAUTILUS)))) {
            ci.cancel();
        }
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

    // ── Crosshair centring ─────────────────────────────────────────────────
    // Vanilla places the 15px crosshair at (guiWidth - 15) / 2 in whole GUI pixels, so its centre sits up to half a
    // GUI pixel (= guiScale / 2 screen pixels) left/up of the real screen centre, more when the window size isn't
    // divisible by the GUI scale. It is centred on the framebuffer instead, snapped to whole screen pixels so the
    // nearest-filtered sprite keeps every line one texel wide. The attack indicator follows the crosshair.

    @Unique private static final int NEATLYBETTER$CROSSHAIR_SIZE = 15;
    /** Vanilla: indicator top is 16-17 GUI px below the crosshair top (parity dependent); use the midpoint. */
    @Unique private static final float NEATLYBETTER$INDICATOR_GAP = 16.5f;

    /** Crosshair left/top edge in screen pixels. */
    @Unique
    private static int neatlybetter$crosshairStart(int windowSize, int scale) {
        return Math.round((windowSize - NEATLYBETTER$CROSSHAIR_SIZE * scale) / 2f);
    }

    /** Indicator left edge in GUI units: centred under the crosshair (16 px wide vs. 15), snapped to screen pixels. */
    @Unique
    private static float neatlybetter$indicatorX(int scale) {
        int windowWidth = Minecraft.getInstance().getWindow().getWidth();
        return Math.round(neatlybetter$crosshairStart(windowWidth, scale) - scale / 2f) / (float) scale;
    }

    @Unique
    private static float neatlybetter$indicatorY(int scale) {
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();
        return Math.round(neatlybetter$crosshairStart(windowHeight, scale) + NEATLYBETTER$INDICATOR_GAP * scale) / (float) scale;
    }

    @Redirect(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 0
            )
    )
    private void neatlybetter$centerCrosshair(GuiGraphicsExtractor graphics,
                                              RenderPipeline renderPipeline, Identifier location,
                                              int x, int y, int width, int height) {
        var window = Minecraft.getInstance().getWindow();
        int scale = window.getGuiScale();
        float fx = neatlybetter$crosshairStart(window.getWidth(), scale) / (float) scale;
        float fy = neatlybetter$crosshairStart(window.getHeight(), scale) / (float) scale;

        ((FloatBlitSprite) graphics).neatlybetter$blitSpriteFloat(renderPipeline, location, fx, fy, width, height);
    }

    /** Attack indicator: full sprite (ordinal 1) and background (ordinal 2). */
    @Redirect(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 1
            )
    )
    private void neatlybetter$centerAttackIndicatorFull(GuiGraphicsExtractor graphics,
                                                        RenderPipeline renderPipeline, Identifier location,
                                                        int x, int y, int width, int height) {
        int scale = Minecraft.getInstance().getWindow().getGuiScale();
        ((FloatBlitSprite) graphics).neatlybetter$blitSpriteFloat(renderPipeline, location,
                neatlybetter$indicatorX(scale), neatlybetter$indicatorY(scale), width, height);
    }

    @Redirect(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 2
            )
    )
    private void neatlybetter$centerAttackIndicatorBackground(GuiGraphicsExtractor graphics,
                                                              RenderPipeline renderPipeline, Identifier location,
                                                              int x, int y, int width, int height) {
        int scale = Minecraft.getInstance().getWindow().getGuiScale();
        ((FloatBlitSprite) graphics).neatlybetter$blitSpriteFloat(renderPipeline, location,
                neatlybetter$indicatorX(scale), neatlybetter$indicatorY(scale), width, height);
    }

    /** Attack indicator progress bar (partial sprite). */
    @Redirect(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V"
            )
    )
    private void neatlybetter$centerAttackIndicatorProgress(GuiGraphicsExtractor graphics,
                                                            RenderPipeline renderPipeline, Identifier location,
                                                            int spriteWidth, int spriteHeight, int textureX, int textureY,
                                                            int x, int y, int width, int height) {
        int scale = Minecraft.getInstance().getWindow().getGuiScale();
        ((FloatBlitSprite) graphics).neatlybetter$blitSpriteFloat(renderPipeline, location,
                spriteWidth, spriteHeight, textureX, textureY,
                neatlybetter$indicatorX(scale), neatlybetter$indicatorY(scale), width, height);
    }

    @Redirect(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"
            )
    )
    private Object neatlybetter$suppressCrosshairAttackIndicator(OptionInstance<?> instance) {
        return NTCommonConfig.CONFIG.legacyCombat.get() ? AttackIndicatorStatus.OFF : instance.get();
    }

    @Redirect(
            method = "extractItemHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"
            )
    )
    private Object neatlybetter$suppressHotbarAttackIndicator(OptionInstance<?> instance) {
        return NTCommonConfig.CONFIG.legacyCombat.get() ? AttackIndicatorStatus.OFF : instance.get();
    }
}