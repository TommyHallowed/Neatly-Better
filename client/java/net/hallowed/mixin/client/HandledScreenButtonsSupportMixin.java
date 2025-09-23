package net.hallowed.mixin.client;

import net.hallowed.client.bridge.OverlayButtonsBridge;
import net.hallowed.client.config.ClientConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Renders custom button backgrounds + icons and handles RMB / Shift+LMB. */
@Mixin(HandledScreen.class)
public abstract class HandledScreenButtonsSupportMixin extends Screen {
    protected HandledScreenButtonsSupportMixin(Text title) { super(title); }

    // Draw custom bg (normal / hover / hidden) and the item icon + tooltip
    @Inject(method = "render", at = @At("TAIL"))
    private void hallowed$renderButtonIcons(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        OverlayButtonsBridge holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();

        if (coords != null) {
            boolean hovered = coords.isMouseOver(mouseX, mouseY);
            boolean visible = ClientConfigManager.coordsVisible();

            hallowed$drawSkinnedButton(ctx, coords, hovered, visible);
            int cx = coords.getX() + (coords.getWidth() - 16) / 2;
            int cy = coords.getY() + (coords.getHeight() - 16) / 2;
            ctx.drawItem(new ItemStack(Items.COMPASS), cx, cy);

            if (hovered) {
                ctx.drawTooltip(this.textRenderer,
                        Text.literal("Coords: LMB toggle\nRMB position\nShift+LMB color"),
                        mouseX, mouseY);
            }
        }

        if (time != null) {
            boolean hovered = time.isMouseOver(mouseX, mouseY);
            boolean visible = ClientConfigManager.timeVisible();

            hallowed$drawSkinnedButton(ctx, time, hovered, visible);
            int tx = time.getX() + (time.getWidth() - 16) / 2;
            int ty = time.getY() + (time.getHeight() - 16) / 2;
            ctx.drawItem(new ItemStack(Items.CLOCK), tx, ty);

            if (hovered) {
                ctx.drawTooltip(this.textRenderer,
                        Text.literal("Time: LMB toggle\nRMB position\nShift+LMB color"),
                        mouseX, mouseY);
            }
        }
    }

    // Shift + LMB = color cycle (consume so the default button press doesn't toggle)
    // RMB = position cycle (unchanged)
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void hallowed$handleClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof InventoryScreen inv)) return;

        OverlayButtonsBridge holder = (OverlayButtonsBridge) inv;
        ButtonWidget coords = holder.hallowed$getCoordsBtn();
        ButtonWidget time   = holder.hallowed$getTimeBtn();
        if (coords == null || time == null) return;

        boolean shift = Screen.hasShiftDown();

        // Shift + Left click -> color
        if (button == 0 && shift) {
            if (hallowed$isWithin(mouseX, mouseY, coords)) {
                ClientConfigManager.cycleCoordsColor();
                cir.setReturnValue(true);
                return;
            }
            if (hallowed$isWithin(mouseX, mouseY, time)) {
                ClientConfigManager.cycleTimeColor();
                cir.setReturnValue(true);
                return;
            }
        }

        // Right click -> position
        if (button == 1) {
            if (hallowed$isWithin(mouseX, mouseY, coords)) {
                ClientConfigManager.cyclePosition(); // (or your per-line version if you added it)
                cir.setReturnValue(true);
                return;
            }
            if (hallowed$isWithin(mouseX, mouseY, time)) {
                ClientConfigManager.cyclePosition(); // (or per-line)
                cir.setReturnValue(true);
            }
        }
    }

    // === helpers ===

    /** Replace vanilla widget visuals with simple skinned rectangles. */
    @Unique
    private static void hallowed$drawSkinnedButton(DrawContext ctx, ButtonWidget b, boolean hovered, boolean visible) {
        int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();

        // Choose background based on state:
        //  - hidden   => muted red
        //  - hovered  => bright gray
        //  - default  => dark gray
        int bg = !visible ? 0xAA8B0000 : (hovered ? 0xAAE0E0E0 : 0xAA2B2B2B);
        int border = hovered ? 0xFFFFFFFF : 0xFF000000;

        // Fill + 1px border
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        ctx.fill(x, y, x + 1, y + h, border);
        ctx.fill(x + w - 1, y, x + w, y + h, border);

        // Optional: dim the icon when hidden
        if (!visible) {
            ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x44000000);
        }
    }

    @Unique
    private static boolean hallowed$isWithin(double mx, double my, ButtonWidget b) {
        return mx >= b.getX() && mx < b.getX() + b.getWidth()
                && my >= b.getY() && my < b.getY() + b.getHeight();
    }
}
