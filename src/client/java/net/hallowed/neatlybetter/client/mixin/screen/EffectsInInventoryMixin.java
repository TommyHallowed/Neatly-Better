package net.hallowed.neatlybetter.client.mixin.screen;

import com.google.common.collect.Ordering;

import net.hallowed.neatlybetter.client.render.EffectBarRenderer;
import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin {

    @Shadow @Final private AbstractContainerScreen<?> screen;

    @Inject(method = "renderEffects", at = @At("TAIL"))
    private void neatlybetter$renderEffectBars(GuiGraphics guiGraphics, Collection<MobEffectInstance> collection, int x, int rowHeight, int mouseX, int mouseY, int maxWidth, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.effectBars.get()) return;

        Iterable<MobEffectInstance> sortedEffects = Ordering.natural().sortedCopy(collection);
        int currentY = this.screen.topPos;
        boolean isWide = maxWidth > 32;

        for (MobEffectInstance effect : sortedEffects) {
            EffectBarRenderer.renderInventory(guiGraphics, effect, x, currentY, maxWidth, isWide);
            currentY += rowHeight;
        }
    }
}