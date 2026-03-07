package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ExperienceBarRenderer.class)
public abstract class ExperienceBarMixin implements ContextualBarRenderer {
    @Shadow @Final private Minecraft minecraft;

    /**
     * @author Hallowed103
     * @reason Overwrites renderBar to include locator icons.
     */
    @Overwrite
    public void renderBackground(@NotNull GuiGraphics context, @NotNull DeltaTracker tickCounter) {
        var player = this.minecraft.player;
        if (player == null) return;

        int centerX = (this.minecraft.getWindow().getGuiScaledWidth() - 182) / 2;
        int y = this.minecraft.getWindow().getGuiScaledHeight() - 32 + 3;
        int nextLevelXp = player.getXpNeededForNextLevel();

        if (nextLevelXp > 0) {
            int progressWidth = (int)(player.experienceProgress * 183.0F);
            context.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                    net.minecraft.client.gui.contextualbar.ExperienceBarRenderer.EXPERIENCE_BAR_BACKGROUND_SPRITE, centerX, y, 182, 5);
            if (progressWidth > 0) {
                context.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                        net.minecraft.client.gui.contextualbar.ExperienceBarRenderer.EXPERIENCE_BAR_PROGRESS_SPRITE,
                        182, 5, 0, 0, centerX, y, progressWidth, 5);
            }
        }
        LocatorBarRenderer locator = new LocatorBarRenderer(this.minecraft);
        locator.render(context, tickCounter);
    }
}
