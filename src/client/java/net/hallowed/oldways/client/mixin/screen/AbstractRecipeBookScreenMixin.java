package net.hallowed.oldways.client.mixin.screen;


import net.hallowed.oldways.client.util.SettingsPrefs;
import net.hallowed.oldways.client.util.SettingsPrefs.RecipeBookMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ClickType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin {

    // --- Recipe Book Mode ---
    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen;initButton()V"
            ),
            cancellable = true
    )
    private void oldways$maybeHideRecipeBookButton(CallbackInfo ci) {
        RecipeBookMode mode = OW$prefs.recipeBookMode;
        if (mode == RecipeBookMode.HIDDEN) {
            ci.cancel();
        }
    }

    // --- Fast Crafting via CTRL + Click ---
    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void oldways$quickCraftOnCtrl(MouseButtonEvent event, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && event.hasControlDown()) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.gameMode != null && mc.player != null) {
                AbstractRecipeBookScreen<?> screen = (AbstractRecipeBookScreen<?>) (Object) this;

                mc.gameMode.handleInventoryMouseClick(
                        screen.getMenu().containerId,
                        0,
                        0,
                        ClickType.QUICK_MOVE,
                        mc.player
                );
            }
        }
    }
}