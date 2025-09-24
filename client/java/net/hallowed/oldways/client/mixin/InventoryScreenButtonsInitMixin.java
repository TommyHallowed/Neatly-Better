package net.hallowed.oldways.client.mixin;

import net.hallowed.oldways.client.mixin.accessor.HandledScreenAccessor;
import net.hallowed.oldways.client.util.OverlayButtonsBridge;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.ui.TextureButtonWidget;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds two invisible (textureless) buttons above the inventory; rendering is done in HandledScreenButtonsSupportMixin. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenButtonsInitMixin extends Screen implements OverlayButtonsBridge {
    protected InventoryScreenButtonsInitMixin(Text title) { super(title); }

    @Unique private ButtonWidget coordsBtn;
    @Unique private ButtonWidget timeBtn;
    @Unique private static final int BTN = 12;

    @Inject(method = "init", at = @At("TAIL"))
    private void hallowed$addOverlayButtons(CallbackInfo ci) {
        var a = (HandledScreenAccessor) this;
        int x  = a.getX();
        int y  = a.getY();
        int bw = a.getBackgroundWidth();

        // place above top-right of the inventory
        int timeX   = x + bw - BTN - 4;
        int coordsX = timeX - BTN - 2;
        int rowY    = y - BTN - 4;

        // Use the simplified TextureButtonWidget (no textures passed here)
        coordsBtn = new TextureButtonWidget(coordsX, rowY, BTN, BTN, Text.empty(),
                b -> ClientConfigManager.toggleCoordsVisible());

        timeBtn   = new TextureButtonWidget(timeX, rowY, BTN, BTN, Text.empty(),
                b -> ClientConfigManager.toggleTimeVisible());

        // Initial visibility based on player inventory / ender chest
        var player = MinecraftClient.getInstance().player;
        boolean hasCompass = player != null &&
                (player.getInventory().contains(Items.COMPASS.getDefaultStack()) || EnderCheckClient.enderHasCompass());
        boolean hasClock   = player != null &&
                (player.getInventory().contains(Items.CLOCK.getDefaultStack())   || EnderCheckClient.enderHasClock());

        coordsBtn.visible = hasCompass;
        timeBtn.visible   = hasClock;

        this.addDrawableChild(coordsBtn);
        this.addDrawableChild(timeBtn);
    }

    @Override public ButtonWidget hallowed$getCoordsBtn() { return coordsBtn; }
    @Override public ButtonWidget hallowed$getTimeBtn()   { return timeBtn; }
}
