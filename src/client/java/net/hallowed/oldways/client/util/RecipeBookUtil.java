package net.hallowed.oldways.client.util;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookUtil {
    private RecipeBookUtil() {}

    public static void closeRecipeBook(ClientRecipeBook book,
                                       ClientPacketListener net,
                                       RecipeBookType category) {
        book.setOpen(category, false);
        net.send(new ServerboundRecipeBookChangeSettingsPacket(category, false, false));
    }
}
