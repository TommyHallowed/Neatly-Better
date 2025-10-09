package net.hallowed.oldways.client.mixin.screen;

import net.hallowed.oldways.client.mixin.accessor.HandledScreenAccessor;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.HudFormatting;
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

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) { super(title); }

    @Inject(method = "render", at = @At("HEAD"))
    private void oldways$updateButtons(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        var a = (HandledScreenAccessor) this;
        final int x  = a.getX();
        final int y  = a.getY();
        final int bw = a.getBackgroundWidth();

        final int btn = 12;
        final int timeX   = x + bw - btn - 4;
        final int coordsX = timeX - btn - 2;
        final int rowY    = y - btn - 4;

        var holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        coords.setPosition(coordsX, rowY);
        time.setPosition(timeX, rowY);

        var player = MinecraftClient.getInstance().player;
        boolean hasCompass = player != null &&
                (player.getInventory().contains(Items.COMPASS.getDefaultStack()) || EnderCheckClient.enderHasCompass());
        boolean hasClock   = player != null &&
                (player.getInventory().contains(Items.CLOCK.getDefaultStack())   || EnderCheckClient.enderHasClock());

        coords.visible = hasCompass;
        time.visible   = hasClock;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void oldways$handleClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        var holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        boolean shift = Screen.hasShiftDown();

        if (button == 0 && shift) {
            if (coords.visible && coords.isMouseOver(mouseX, mouseY)) {
                HudFormatting.cycleCoordsColor();
                cir.setReturnValue(true);
                return;
            }
            if (time.visible && time.isMouseOver(mouseX, mouseY)) {
                HudFormatting.cycleTimeColor();
                cir.setReturnValue(true);
                return;
            }
        }

        if (button == 1) {
            if (coords.visible && coords.isMouseOver(mouseX, mouseY)) {
                HudFormatting.cycleCoordsPosition();
                cir.setReturnValue(true);
                return;
            }
            if (time.visible && time.isMouseOver(mouseX, mouseY)) {
                HudFormatting.cycleTimePosition();
                cir.setReturnValue(true);
            }
        }

    }
}
