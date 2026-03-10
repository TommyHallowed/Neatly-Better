package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBarRenderer.class)
public abstract class ExperienceBarMixin implements ContextualBarRenderer {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private LocatorBarRenderer oldways$locator;

    /**
     * Append locator bar rendering after the vanilla XP bar.
     * @Inject at TAIL is compatible with other mods — unlike @Overwrite.
     */
    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void oldways$appendLocatorBar(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (oldways$locator == null) {
            oldways$locator = new LocatorBarRenderer(this.minecraft);
        }
        oldways$locator.render(context, tickCounter);
    }
}