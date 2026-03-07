package net.hallowed.oldways.client.mixin.ui.locatorbar;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(JumpableVehicleBarRenderer.class)
public abstract class JumpBarMixin implements ContextualBarRenderer {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private PlayerRideableJumping playerJumpableVehicle;

    @Unique private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("hud/jump_bar_background");
    @Unique private static final Identifier COOLDOWN = Identifier.withDefaultNamespace("hud/jump_bar_cooldown");
    @Unique private static final Identifier PROGRESS = Identifier.withDefaultNamespace("hud/jump_bar_progress");

    /**
     * @author Hallowed103
     * @reason Overwrites the Jump bar render to include locator icons.
     */
    @Overwrite
    public void renderBackground(@NotNull GuiGraphics context, @NotNull DeltaTracker tickCounter) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        int centerX = this.left(this.minecraft.getWindow());
        int centerY = this.top(this.minecraft.getWindow());

        context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, centerX, centerY, 182, 5);

        if (this.playerJumpableVehicle != null && this.playerJumpableVehicle.getJumpCooldown() > 0) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, COOLDOWN, centerX, centerY, 182, 5);
        } else {
            float strength = this.minecraft.player.getJumpRidingScale();
            if (strength > 0.0F) {
                int progress = (int)(strength * 183.0F);
                context.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS, 182, 5, 0, 0, centerX, centerY, progress, 5);
            }
        }

        LocatorBarRenderer locator = new LocatorBarRenderer(this.minecraft);
        locator.render(context, tickCounter);
    }
}
