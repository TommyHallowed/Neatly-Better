package net.hallowed.neatlybetter.client.mixin.screen;

import com.google.common.collect.Ordering;
import net.hallowed.neatlybetter.client.render.EffectBarRenderer;
import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
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
    private void neatlybetter$renderHUDEffectBars(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.effectBars.get()) return;

        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty()) return;

        int beneficialCount = 0;
        int harmfulCount = 0;

        for (MobEffectInstance effect : Ordering.natural().reverse().sortedCopy(collection)) {
            if (!effect.showIcon()) continue;

            boolean isBeneficial = effect.getEffect().value().isBeneficial();
            int x = guiGraphics.guiWidth();
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

            EffectBarRenderer.renderHUD(guiGraphics, effect, x, y);
        }
    }

    @Inject(method = "renderAirBubbles", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$hideAirBubblesWithWaterBreathing(
            GuiGraphics guiGraphics, Player player, int i, int j, int k, CallbackInfo ci) {
        if (player.hasEffect(MobEffects.WATER_BREATHING)) {
            ci.cancel();
        }
    }
}