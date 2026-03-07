package net.hallowed.oldways.client.mixin.screen;

import net.hallowed.oldways.client.util.SettingsPrefs;
import net.hallowed.oldways.client.util.SettingsPrefs.RecipeBookMode;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class RecipeBookScreenMixin {

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
}
