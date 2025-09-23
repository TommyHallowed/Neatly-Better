package net.hallowed.oldways.content;

import net.hallowed.TheOldWays;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ContentRegistry {
    private ContentRegistry() {}

    public static Identifier id(String path) { return Identifier.of(TheOldWays.MOD_ID, path); }

    /** Register a Block and its BlockItem. Returns the Block. */
    public static <T extends Block> T blockWithItem(String path, T block, Item.Settings itemSettings) {
        Registry.register(Registries.BLOCK, id(path), block);
        Registry.register(Registries.ITEM,  id(path), new BlockItem(block, itemSettings));
        return block;
    }

    /** Register a plain Item. */
    public static Item item(String path, Item item) {
        return Registry.register(Registries.ITEM, id(path), item);
    }
}
