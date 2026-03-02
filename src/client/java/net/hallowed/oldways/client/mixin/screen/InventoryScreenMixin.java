package net.hallowed.oldways.client.mixin.screen;

import net.hallowed.oldways.client.feature.ui.TextureButtonWidget;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DataFlowIssue")
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends Screen {
    protected InventoryScreenMixin(Text title) { super(title); }

    @Unique private ButtonWidget coordsBtn;
    @Unique private ButtonWidget timeBtn;
    @Unique private static final int BTN = 12;
    @Unique private static final int SHIFT = 77;

    @Unique private int baseCoordsX;
    @Unique private int baseTimeX;
    @Unique private int rowY;

    @Inject(method = "init", at = @At("TAIL"))
    private void hallowed$addOverlayButtons(CallbackInfo ci) {
        HandledScreen<?> handled = (HandledScreen<?>) (Object) this;
        int x  = handled.x;
        int y  = handled.y;
        int bw = handled.backgroundWidth;

        boolean bookOpen = false;
        try {
            RecipeBookWidget<?> rb = ((RecipeBookScreen<?>) (Object) this).recipeBook;
            bookOpen = rb != null && rb.isOpen();
        } catch (Throwable ignored) {
        }

        int timeXNow   = x + bw - BTN - 4;
        int coordsXNow = timeXNow - BTN - 2;
        rowY           = y - BTN - 2;

        baseCoordsX = bookOpen ? (coordsXNow - SHIFT) : coordsXNow;
        baseTimeX   = bookOpen ? (timeXNow   - SHIFT) : timeXNow;

        coordsBtn = new TextureButtonWidget(
                baseCoordsX, rowY, BTN, BTN,
                Identifier.of("old-ways", "textures/gui/overlay/compass_icon_shown.png"),
                Identifier.of("old-ways", "textures/gui/overlay/compass_icon_hidden.png"),
                () -> SettingsPrefs.get().showCoords,
                b -> { var p = SettingsPrefs.get(); p.showCoords = !p.showCoords; SettingsPrefs.save(); }
        );

        timeBtn = new TextureButtonWidget(
                baseTimeX, rowY, BTN, BTN,
                Identifier.of("old-ways", "textures/gui/overlay/clock_icon_shown.png"),
                Identifier.of("old-ways", "textures/gui/overlay/clock_icon_hidden.png"),
                () -> SettingsPrefs.get().showTime,
                b -> { var p = SettingsPrefs.get(); p.showTime = !p.showTime; SettingsPrefs.save(); }
        );

        var player = MinecraftClient.getInstance().player;
        boolean hasCompass = player != null && (InventoryDeepScan.hasCompass(player) || EnderCheckClient.enderHasCompass());
        boolean hasClock   = player != null && (InventoryDeepScan.hasClock(player)   || EnderCheckClient.enderHasClock());
        coordsBtn.visible = hasCompass;
        timeBtn.visible   = hasClock;

        this.addDrawableChild(coordsBtn);
        this.addDrawableChild(timeBtn);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void hallowed$shiftButtonsWithRecipeBook(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (coordsBtn == null || timeBtn == null) return;

        int shift = 0;
        try {
            RecipeBookWidget<?> book = ((RecipeBookScreen<?>) (Object) this).recipeBook;
            if (book != null && book.isOpen()) shift = SHIFT;
        } catch (Throwable ignored) { }

        var mc = MinecraftClient.getInstance();
        var player = mc.player;
        boolean hasCompass = player != null && (InventoryDeepScan.hasCompass(player) || EnderCheckClient.enderHasCompass());
        boolean hasClock   = player != null && (InventoryDeepScan.hasClock(player)   || EnderCheckClient.enderHasClock());
        coordsBtn.visible = hasCompass;
        timeBtn.visible   = hasClock;

        int coordsX = baseCoordsX + shift;
        int timeX   = baseTimeX   + shift;

        if (coordsBtn.visible && !timeBtn.visible) {
            coordsX = timeX;
        }

        coordsBtn.setX(coordsX);
        coordsBtn.setY(rowY);
        timeBtn.setX(timeX);
        timeBtn.setY(rowY);
    }
}
