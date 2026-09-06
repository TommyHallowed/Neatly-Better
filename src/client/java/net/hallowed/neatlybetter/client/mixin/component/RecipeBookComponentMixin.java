package net.hallowed.neatlybetter.client.mixin.component;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {
    @Shadow
    protected Minecraft minecraft;

    @ModifyReturnValue(method = "updateScreenPosition", at = @At("RETURN"))
    private int NeatlyBetter$removeRecipeBookShift(int original, int width, int imageWidth) {
        return !NTClientConfig.CONFIG.recipeBookShift.get() ? (width - imageWidth) / 2 : original;
    }

    @ModifyReturnValue(method = "getXOrigin", at = @At("RETURN"))
    private int NeatlyBetter$changeRecipeBookPosition(int original) {
        return !NTClientConfig.CONFIG.recipeBookShift.get() ? original - 77 : original;
    }

    @ModifyArg(method = "updateTabs", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookTabButton;setPosition(II)V"), index = 0)
    private int NeatlyBetter$changeRecipeBookTabButtonPosition(int original) {
        return !NTClientConfig.CONFIG.recipeBookShift.get() ? original - 77 : original;
    }
}
