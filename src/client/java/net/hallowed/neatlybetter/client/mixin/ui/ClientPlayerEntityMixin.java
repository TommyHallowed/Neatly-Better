package net.hallowed.neatlybetter.client.mixin.ui;

import net.hallowed.neatlybetter.client.util.RecipeBookUtil;
import net.hallowed.neatlybetter.client.util.SettingsPrefs;
import net.hallowed.neatlybetter.client.util.SettingsPrefs.RecipeBookMode;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow @Final private ClientRecipeBook recipeBook;
    @Shadow @Final public ClientPacketListener connection;

    @Unique
    private static final SettingsPrefs neatlybetter$prefs = SettingsPrefs.get();

    @Inject(method = "clientSideCloseContainer", at = @At("HEAD"))
    private void neatlybetter$closeRecipeBookIfOpen(CallbackInfo ci) {
        RecipeBookMode mode = neatlybetter$prefs.recipeBookMode;
        if (mode == RecipeBookMode.SHOWN) return;

        Minecraft client = Minecraft.getInstance();
        if (client.screen == null) return;

        if (client.screen instanceof AbstractRecipeBookScreen<?> recipeScreen) {
            RecipeBookComponent<?> recipeBookWidget = recipeScreen.recipeBookComponent;

            RecipeBookMenu handler = recipeBookWidget.menu;

            RecipeBookType category = handler.getRecipeBookType();
            RecipeBookUtil.closeRecipeBook(this.recipeBook, this.connection, category);
        }
    }
}
