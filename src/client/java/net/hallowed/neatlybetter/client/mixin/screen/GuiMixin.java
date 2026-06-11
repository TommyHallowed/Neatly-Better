package net.hallowed.neatlybetter.client.mixin.screen;

import com.google.common.collect.Ordering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.client.render.EffectBarRenderer;
import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.hallowed.neatlybetter.client.util.FloatBlitSprite;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(Gui.class)
public abstract class GuiMixin {

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

    @Redirect(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 0
            )
    )
    private void neatlybetter$centerCrosshair(GuiGraphicsExtractor graphics,
                                              RenderPipeline renderPipeline, Identifier location,
                                              int x, int y, int width, int height) {
        float scale      = (float) Minecraft.getInstance().getWindow().getGuiScale();
        float scaledCenterX = (Minecraft.getInstance().getWindow().getWidth()  / scale) / 2f;
        float scaledCenterY = (Minecraft.getInstance().getWindow().getHeight() / scale) / 2f;

        float fx = Math.round((scaledCenterX - 7.5f) * 4) / 4f;
        float fy = Math.round((scaledCenterY - 7.5f) * 4) / 4f;

        ((FloatBlitSprite) graphics).neatlybetter$blitSpriteFloat(renderPipeline, location, fx, fy, 15, 15);
    }
}
