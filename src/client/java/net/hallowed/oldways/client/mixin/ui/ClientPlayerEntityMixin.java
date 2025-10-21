package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.mixin.accessor.RecipeBookAccessor;
import net.hallowed.oldways.client.mixin.accessor.RecipeBookScreenAccessor;
import net.hallowed.oldways.client.util.RecipeBookUtil;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.hallowed.oldways.client.util.SettingsPrefs.RecipeBookMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow @Final private ClientRecipeBook recipeBook;
    @Shadow @Final public ClientPlayNetworkHandler networkHandler;
    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Inject(method = "closeScreen", at = @At("HEAD"))
    private void oldways$closeRecipeBookIfOpen(CallbackInfo ci) {
        RecipeBookMode mode = OW$prefs.recipeBookMode;

        if (mode == RecipeBookMode.SHOWN) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.currentScreen == null) return;

        if (client.currentScreen instanceof RecipeBookScreen<?> recipeScreen) {
            RecipeBookWidget<?> widget = ((RecipeBookScreenAccessor) recipeScreen).getRecipeBook();
            RecipeBookType category = ((RecipeBookAccessor) widget).getCraftingHandler().getCategory();
            RecipeBookUtil.closeRecipeBook(this.recipeBook, this.networkHandler, category);
        }
    }
}
