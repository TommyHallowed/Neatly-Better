package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When enabled, prevents the recipe-book toggle button from being added to the
 * Survival Inventory screen. The recipe book widget itself still exists, so the
 * book can be opened by other means if needed.
 */
@Mixin(RecipeBookScreen.class)
public abstract class RecipeBookScreenMixin {

    /** Intercept the call inside init() that adds the recipe-book button. */
    @Inject(
            method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/RecipeBookScreen;addRecipeBook()V"),
            cancellable = true
    )
    private void oldways$maybeHideRecipeBookButton(CallbackInfo ci) {
        if (ClientConfigManager.recipeBookHideButtonEnabled()) {
            ci.cancel();
        }
    }
}
