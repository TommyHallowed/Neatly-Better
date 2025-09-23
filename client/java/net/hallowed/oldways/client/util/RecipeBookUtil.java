package net.hallowed.oldways.client.util;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.book.RecipeBookType;

public final class RecipeBookUtil {
    private RecipeBookUtil() {}

    public static void closeRecipeBook(ClientRecipeBook book,
                                       ClientPlayNetworkHandler net,
                                       RecipeBookType category) {
        // close on client
        book.setGuiOpen(category, false);
        // inform server so it stays closed next open
        net.sendPacket(new RecipeCategoryOptionsC2SPacket(category, false, false));
    }
}
