// src/client/java/net/hallowed/oldways/client/mixin/HandledScreenButtonsSupportMixin.java
package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.mixin.accessor.HandledScreenAccessor;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.OverlayButtonsBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the overlay buttons in sync with InventoryScreen layout (with or without
 * recipe book), updates visibility based on held items, and handles RMB/Shift+LMB.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) { super(title); }

    /** Recompute positions + visibility every frame. */
    @Inject(method = "render", at = @At("HEAD"))
    private void oldways$updateButtons(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        // Access the current x/y/backgroundWidth of the InventoryScreen.
        var a = (HandledScreenAccessor) this;
        final int x  = a.getX();
        final int y  = a.getY();
        final int bw = a.getBackgroundWidth();

        // Where our buttons should live *this* frame.
        final int btn = 12; // keep in sync with InventoryScreenButtonsInitMixin
        final int timeX   = x + bw - btn - 4;
        final int coordsX = timeX - btn - 2;
        final int rowY    = y - btn - 4;

        var holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        // Move them to the fresh positions (this follows the recipe-book slide automatically).
        coords.setPosition(coordsX, rowY);
        time.setPosition(timeX, rowY);

        // Gate their visibility by items (inventory or ender chest).
        var player = MinecraftClient.getInstance().player;
        boolean hasCompass = player != null &&
                (player.getInventory().contains(Items.COMPASS.getDefaultStack()) || EnderCheckClient.enderHasCompass());
        boolean hasClock   = player != null &&
                (player.getInventory().contains(Items.CLOCK.getDefaultStack())   || EnderCheckClient.enderHasClock());

        coords.visible = hasCompass;
        time.visible   = hasClock;
    }

    /** RMB cycles position; Shift+LMB cycles color. Ignore when hidden. */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void oldways$handleClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        var holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        boolean shift = Screen.hasShiftDown();

        if (button == 0 && shift) { // Shift + LMB => color
            if (coords.visible && coords.isMouseOver(mouseX, mouseY)) {
                ClientConfigManager.cycleCoordsColor();
                cir.setReturnValue(true);
                return;
            }
            if (time.visible && time.isMouseOver(mouseX, mouseY)) {
                ClientConfigManager.cycleTimeColor();
                cir.setReturnValue(true);
                return;
            }
        }

        if (button == 1) { // RMB => position
            if (coords.visible && coords.isMouseOver(mouseX, mouseY)) {
                ClientConfigManager.cycleCoordsPosition();
                cir.setReturnValue(true);
                return;
            }
            if (time.visible && time.isMouseOver(mouseX, mouseY)) {
                ClientConfigManager.cycleTimePosition();
                cir.setReturnValue(true);
            }
        }
    }
}
