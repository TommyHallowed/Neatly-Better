package net.hallowed.oldways.client.mixin;

import net.hallowed.oldways.client.mixin.accessor.HandledScreenAccessor;
import net.hallowed.oldways.client.util.OverlayButtonsBridge;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds two vanilla buttons to the Survival Inventory (top-right). */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenButtonsInitMixin extends Screen implements OverlayButtonsBridge {
    protected InventoryScreenButtonsInitMixin(Text title) { super(title); }

    @Unique private ButtonWidget coordsBtn;
    @Unique private ButtonWidget timeBtn;
    @Unique private static final int BTN = 10;

    @Inject(method = "init", at = @At("TAIL"))
    private void hallowed$addOverlayButtons(CallbackInfo ci) {
        // Read layout from HandledScreen via accessor
        var a = (HandledScreenAccessor) this;
        int x  = a.getX();
        int y  = a.getY();
        int bw = a.getBackgroundWidth();

        // Right edge: [coords][time]
        int timeX   = x + bw - BTN - 4;   // 4px padding from right
        int coordsX = timeX - BTN - 2;    // 2px gap
        int rowY    = y + 4;              // 4px from top

        coordsBtn = ButtonWidget.builder(Text.empty(), b -> {
            // LMB toggles coords line visibility
            ClientConfigManager.toggleCoordsVisible();
        }).dimensions(coordsX, rowY, BTN, BTN).build();

        timeBtn = ButtonWidget.builder(Text.empty(), b -> {
            // LMB toggles time line visibility
            ClientConfigManager.toggleTimeVisible();
        }).dimensions(timeX, rowY, BTN, BTN).build();

        // protected on Screen; allowed because this mixin extends Screen
        this.addDrawableChild(coordsBtn);
        this.addDrawableChild(timeBtn);
    }

    // Bridge getters for the support mixin
    @Override public ButtonWidget hallowed$getCoordsBtn() { return coordsBtn; }
    @Override public ButtonWidget hallowed$getTimeBtn()   { return timeBtn; }
}
