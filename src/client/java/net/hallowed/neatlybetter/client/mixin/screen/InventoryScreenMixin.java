package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.client.feature.ui.TextureButtonWidget;
import net.hallowed.neatlybetter.client.util.*;
import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends Screen {
    protected InventoryScreenMixin(Component title) { super(title); }

    @Unique private Button coordsBtn;
    @Unique private Button timeBtn;
    @Unique private static final int BTN = 12;
    @Unique private static final int SHIFT = 77;

    @Unique private int baseCoordsX;
    @Unique private int baseTimeX;
    @Unique private int rowY;

    @Inject(method = "init", at = @At("TAIL"))
    private void hallowed$addOverlayButtons(CallbackInfo ci) {
        AbstractContainerScreen<?> handled = (AbstractContainerScreen<?>) (Object) this;
        int x  = handled.leftPos;
        int y  = handled.topPos;
        int bw = handled.imageWidth;

        boolean bookOpen = false;
        try {
            RecipeBookComponent<?> rb = ((AbstractRecipeBookScreen<?>) (Object) this).recipeBookComponent;
            bookOpen = rb != null && rb.isVisible();
        } catch (Throwable ignored) {
        }

        int timeXNow   = x + bw - BTN - 4;
        int coordsXNow = timeXNow - BTN - 2;
        rowY           = y - BTN - 2;

        baseCoordsX = bookOpen ? (coordsXNow - SHIFT) : coordsXNow;
        baseTimeX   = bookOpen ? (timeXNow   - SHIFT) : timeXNow;

        coordsBtn = new TextureButtonWidget(
                baseCoordsX, rowY, BTN, BTN,
                ModTextures.COMPASS_SHOWN,
                ModTextures.COMPASS_HIDDEN,
                NTClientConfig.CONFIG.showCoords::get,
                b -> NTClientConfig.CONFIG.showCoords.set(!NTClientConfig.CONFIG.showCoords.get())
        );

        timeBtn = new TextureButtonWidget(
                baseTimeX, rowY, BTN, BTN,
                ModTextures.CLOCK_SHOWN,
                ModTextures.CLOCK_HIDDEN,
                NTClientConfig.CONFIG.showTime::get,
                b -> NTClientConfig.CONFIG.showTime.set(!NTClientConfig.CONFIG.showTime.get())
        );

        var player = Minecraft.getInstance().player;
        boolean hasCompass = player != null && (InventoryDeepScan.hasCompass(player) || EnderCheckClient.enderHasCompass() || BackpackCheckClient.backpackHasCompass());
        boolean hasClock   = player != null && (InventoryDeepScan.hasClock(player)   || EnderCheckClient.enderHasClock()   || BackpackCheckClient.backpackHasClock());
        coordsBtn.visible = hasCompass;
        timeBtn.visible   = hasClock;

        this.addRenderableWidget(coordsBtn);
        this.addRenderableWidget(timeBtn);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void hallowed$shiftButtonsWithRecipeBook(GuiGraphics ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (coordsBtn == null || timeBtn == null) return;

        int shift = 0;
        try {
            RecipeBookComponent<?> book = ((AbstractRecipeBookScreen<?>) (Object) this).recipeBookComponent;
            if (book != null && book.isVisible()) shift = SHIFT;
        } catch (Throwable ignored) { }

        var mc = Minecraft.getInstance();
        var player = mc.player;
        boolean hasCompass = player != null && (InventoryDeepScan.hasCompass(player) || EnderCheckClient.enderHasCompass() || BackpackCheckClient.backpackHasCompass());
        boolean hasClock   = player != null && (InventoryDeepScan.hasClock(player)   || EnderCheckClient.enderHasClock()   || BackpackCheckClient.backpackHasClock());
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