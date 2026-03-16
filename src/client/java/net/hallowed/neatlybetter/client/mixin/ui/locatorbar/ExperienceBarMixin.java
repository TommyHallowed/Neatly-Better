package net.hallowed.neatlybetter.client.mixin.ui.locatorbar;

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
    private LocatorBarRenderer neatlybetter$locator;

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void neatlybetter$appendLocatorBar(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (neatlybetter$locator == null) {
            neatlybetter$locator = new LocatorBarRenderer(this.minecraft);
        }
        neatlybetter$locator.render(context, tickCounter);
    }
}