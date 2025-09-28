package net.hallowed.oldways.client.mixin.accessor;

import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookWidget.class)
public interface RecipeBookAccessor {
    @Accessor("craftingScreenHandler")
    AbstractRecipeScreenHandler getCraftingHandler();
}
