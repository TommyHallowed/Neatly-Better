package net.hallowed.neatlybetter.client.mixin.entity.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.neatlybetter.client.util.RecipeBookUtil;
import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.KeyMapping;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow @Final private ClientRecipeBook recipeBook;
    @Shadow @Final public ClientPacketListener connection;

    @WrapOperation(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;resetToggleKeys()V"
            )
    )
    private void neatlybetter$preserveSprintToggleOnRespawn(Operation<Void> original) {
        KeyMapping sprintKey = Minecraft.getInstance().options.keySprint;
        boolean wasSprintToggled = sprintKey.isDown();

        original.call();

        if (wasSprintToggled) {
            sprintKey.setDown(true);
        }
    }

    @Inject(method = "clientSideCloseContainer", at = @At("HEAD"))
    private void neatlybetter$closeRecipeBookIfOpen(CallbackInfo ci) {
        if (NTClientConfig.CONFIG.recipeBookMode.get() == NTClientConfig.RecipeBookMode.SHOWN) return;

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
