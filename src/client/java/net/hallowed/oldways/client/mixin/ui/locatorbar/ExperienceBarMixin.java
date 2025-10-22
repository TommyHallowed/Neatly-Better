package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin implements Bar {
    @Shadow @Final private MinecraftClient client;

    /**
     * @author Hallowed103
     * @reason Overwrites renderBar to include locator icons.
     */
    @Overwrite
    public void renderBar(DrawContext context, RenderTickCounter tickCounter) {
        var player = this.client.player;
        if (player == null) return;

        int centerX = (this.client.getWindow().getScaledWidth() - 182) / 2;
        int y = this.client.getWindow().getScaledHeight() - 32 + 3;
        int nextLevelXp = player.getNextLevelExperience();

        if (nextLevelXp > 0) {
            int progressWidth = (int)(player.experienceProgress * 183.0F);
            context.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                    net.minecraft.client.gui.hud.bar.ExperienceBar.BACKGROUND, centerX, y, 182, 5);
            if (progressWidth > 0) {
                context.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                        net.minecraft.client.gui.hud.bar.ExperienceBar.PROGRESS,
                        182, 5, 0, 0, centerX, y, progressWidth, 5);
            }
        }
        LocatorBar locator = new LocatorBar(this.client);
        locator.renderAddons(context, tickCounter);
    }
}
