package net.hallowed.oldways.client.mixin;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.OverlayButtonsBridge;
import net.hallowed.oldways.client.ui.GuiSprites;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws GUI-atlas sprites for the overlay buttons (icon + tinted outline),
 * hides buttons when the required item is missing, and handles RMB/Shift+LMB.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenButtonsSupportMixin extends Screen {
    protected HandledScreenButtonsSupportMixin(Text title) { super(title); }

    // --- visibility sync (requires compass/clock either in inv or ender chest) ---
    @Inject(method = "render", at = @At("HEAD"))
    private void oldways$syncVisibility(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        OverlayButtonsBridge holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        var player = MinecraftClient.getInstance().player;
        boolean hasCompass = player != null
                && (player.getInventory().contains(Items.COMPASS.getDefaultStack()) || EnderCheckClient.enderHasCompass());
        boolean hasClock   = player != null
                && (player.getInventory().contains(Items.CLOCK.getDefaultStack())   || EnderCheckClient.enderHasClock());

        coords.visible = hasCompass;
        time.visible   = hasClock;
    }

    // --- draw our sprites after vanilla buttons have positioned themselves ---
    @Inject(method = "render", at = @At("TAIL"))
    private void oldways$renderButtons(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        OverlayButtonsBridge holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        if (coords.visible) {
            boolean shown   = ClientConfigManager.coordsVisible();
            boolean hovered = coords.isMouseOver(mouseX, mouseY);
            oldways$drawIconWithOutline(ctx,
                    coords.getX(), coords.getY(), coords.getWidth(), coords.getHeight(),
                    shown ? GuiSprites.COMPASS_SHOWN : GuiSprites.COMPASS_HIDDEN,
                    GuiSprites.COMPASS_OUTLINE,
                    ClientConfigManager.coordsColorARGB(), shown, hovered);
        }

        if (time.visible) {
            boolean shown   = ClientConfigManager.timeVisible();
            boolean hovered = time.isMouseOver(mouseX, mouseY);
            oldways$drawIconWithOutline(ctx,
                    time.getX(), time.getY(), time.getWidth(), time.getHeight(),
                    shown ? GuiSprites.CLOCK_SHOWN : GuiSprites.CLOCK_HIDDEN,
                    GuiSprites.CLOCK_OUTLINE,
                    ClientConfigManager.timeColorARGB(), shown, hovered);
        }
    }

    // RMB cycles position, Shift+LMB cycles color
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void oldways$handleClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        OverlayButtonsBridge holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        boolean shift = Screen.hasShiftDown();

        if (button == 0 && shift) { // Shift + LMB => color
            if (coords.visible && coords.isMouseOver(mouseX, mouseY)) { ClientConfigManager.cycleCoordsColor();   cir.setReturnValue(true); return; }
            if (time.visible   && time.isMouseOver(mouseX, mouseY))   { ClientConfigManager.cycleTimeColor();     cir.setReturnValue(true); return; }
        }

        if (button == 1) { // RMB => position
            if (coords.visible && coords.isMouseOver(mouseX, mouseY)) { ClientConfigManager.cycleCoordsPosition(); cir.setReturnValue(true); return; }
            if (time.visible   && time.isMouseOver(mouseX, mouseY))   { ClientConfigManager.cycleTimePosition();   cir.setReturnValue(true);
            }
        }
    }

    /** Center a 16x16 icon in the button rect and draw optional tinted outline. */
    @Unique
    private static void oldways$drawIconWithOutline(
            DrawContext ctx, int x, int y, int w, int h,
            Identifier faceSprite, Identifier outlineSprite,
            int argbColor, boolean lineShown, boolean hovered
    ) {
        int ix = x + (w - 16) / 2;
        int iy = y + (h - 16) / 2;

        // face (no tint)
        ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, faceSprite, ix, iy, 16, 16);

        // outline (tinted) — only when that overlay line is enabled
        if (lineShown) {
            int color = argbColor;
            if (hovered) {
                // strengthen alpha on hover
                color = (0xCC << 24) | (argbColor & 0x00FFFFFF);
            }
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, outlineSprite, ix, iy, 16, 16, color);
        }
    }
}
