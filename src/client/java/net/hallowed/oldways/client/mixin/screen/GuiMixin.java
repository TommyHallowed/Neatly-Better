package net.hallowed.oldways.client.mixin.screen;

import com.google.common.collect.Ordering;
import net.hallowed.oldways.client.render.EffectBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @Inject(method = "renderEffects", at = @At("TAIL"))
    private void oldways$renderHUDEffectBars(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty()) return;

        int beneficialCount = 0;
        int harmfulCount = 0;

        // HUD renders effects from right to left, using natural ordering reversed!
        for (MobEffectInstance effect : Ordering.natural().reverse().sortedCopy(collection)) {
            if (!effect.showIcon()) continue;

            boolean isBeneficial = effect.getEffect().value().isBeneficial();
            int x = guiGraphics.guiWidth();
            int y = 1;

            if (this.minecraft.isDemo()) {
                y += 15;
            }

            // Exactly mimic Vanilla's coordinate spacing to overlay the bar perfectly
            if (isBeneficial) {
                beneficialCount++;
                x -= 25 * beneficialCount;
            } else {
                harmfulCount++;
                x -= 25 * harmfulCount;
                y += 26; // Harmful effects render in the bottom row
            }

            EffectBarRenderer.renderHUD(guiGraphics, effect, x, y);
        }
    }
}