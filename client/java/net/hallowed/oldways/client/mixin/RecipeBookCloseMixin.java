package net.hallowed.oldways.client.mixin;

import net.hallowed.oldways.client.mixin.accessor.RecipeBookAccessor;
import net.hallowed.oldways.client.mixin.accessor.RecipeBookScreenAccessor;
import net.hallowed.oldways.client.util.RecipeBookUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.recipe.book.RecipeBookType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class RecipeBookCloseMixin {
    @Shadow @Final private ClientRecipeBook recipeBook;
    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    // In 1.21.8 this method is still ClientPlayerEntity#closeScreen()
    @Inject(method = "closeScreen", at = @At("HEAD"))
    private void oldways$closeRecipeBookIfOpen(CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null || client.currentScreen == null) return;

        if (client.currentScreen instanceof RecipeBookScreen<?> recipeScreen) {
            RecipeBookWidget<?> widget =
                    ((RecipeBookScreenAccessor) recipeScreen).getRecipeBook();
            RecipeBookType category =
                    ((RecipeBookAccessor) widget).getCraftingHandler().getCategory();

            RecipeBookUtil.closeRecipeBook(recipeBook, networkHandler, category);
        }
    }
}
