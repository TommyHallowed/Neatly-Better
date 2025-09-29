package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.mixin.accessor.HandledScreenAccessor;
import net.hallowed.oldways.client.util.OverlayButtonsBridge;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.ui.TextureButtonWidget;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds two texture-only buttons to the Survival Inventory header area. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends Screen implements OverlayButtonsBridge {
    protected InventoryScreenMixin(Text title) { super(title); }

    @Unique private ButtonWidget coordsBtn;
    @Unique private ButtonWidget timeBtn;
    @Unique private static final int BTN = 12;

    @Inject(method = "init", at = @At("TAIL"))
    private void hallowed$addOverlayButtons(CallbackInfo ci) {
        var a = (HandledScreenAccessor) this;
        int x  = a.getX();
        int y  = a.getY();
        int bw = a.getBackgroundWidth();

        int timeX   = x + bw - BTN - 4;
        int coordsX = timeX - BTN - 2;
        int rowY    = y - BTN - 4;

        coordsBtn = new TextureButtonWidget(
                coordsX, rowY, BTN, BTN,
                Identifier.of("old-ways", "textures/gui/overlay/compass_icon_shown.png"),
                Identifier.of("old-ways", "textures/gui/overlay/compass_icon_hidden.png"),
                ClientConfigManager::coordsVisible,
                b -> ClientConfigManager.toggleCoordsVisible()
        );

        timeBtn = new TextureButtonWidget(
                timeX, rowY, BTN, BTN,
                Identifier.of("old-ways", "textures/gui/overlay/clock_icon_shown.png"),
                Identifier.of("old-ways", "textures/gui/overlay/clock_icon_hidden.png"),
                ClientConfigManager::timeVisible,
                b -> ClientConfigManager.toggleTimeVisible()
        );

        var player = MinecraftClient.getInstance().player;
        boolean hasCompass = player != null && (InventoryDeepScan.hasCompass(player) || EnderCheckClient.enderHasCompass());
        boolean hasClock   = player != null && (InventoryDeepScan.hasClock(player)   || EnderCheckClient.enderHasClock());

        coordsBtn.visible = hasCompass;
        timeBtn.visible   = hasClock;

        this.addDrawableChild(coordsBtn);
        this.addDrawableChild(timeBtn);
    }

    @Override public ButtonWidget hallowed$getCoordsBtn() { return coordsBtn; }
    @Override public ButtonWidget hallowed$getTimeBtn()   { return timeBtn; }
}
