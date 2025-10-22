package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.JumpBar;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.JumpingMount;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(JumpBar.class)
public abstract class JumpBarMixin implements Bar {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private JumpingMount jumpingMount;

    @Unique private static final Identifier BACKGROUND = Identifier.ofVanilla("hud/jump_bar_background");
    @Unique private static final Identifier COOLDOWN = Identifier.ofVanilla("hud/jump_bar_cooldown");
    @Unique private static final Identifier PROGRESS = Identifier.ofVanilla("hud/jump_bar_progress");

    /**
     * @author Hallowed103
     * @reason Overwrites the Jump bar render to include locator icons.
     */
    @Overwrite
    public void renderBar(DrawContext context, RenderTickCounter tickCounter) {
        if (this.client == null || this.client.player == null) return;

        int centerX = this.getCenterX(this.client.getWindow());
        int centerY = this.getCenterY(this.client.getWindow());

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, centerX, centerY, 182, 5);

        if (this.jumpingMount != null && this.jumpingMount.getJumpCooldown() > 0) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, COOLDOWN, centerX, centerY, 182, 5);
        } else {
            float strength = this.client.player.getMountJumpStrength();
            if (strength > 0.0F) {
                int progress = (int)(strength * 183.0F);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PROGRESS, 182, 5, 0, 0, centerX, centerY, progress, 5);
            }
        }

        LocatorBar locator = new LocatorBar(this.client);
        locator.renderAddons(context, tickCounter);
    }
}
