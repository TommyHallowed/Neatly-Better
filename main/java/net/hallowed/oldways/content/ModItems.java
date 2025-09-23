// src/main/java/net/hallowed/oldways/content/ModItems.java
package net.hallowed.oldways.content;

import net.hallowed.TheOldWays;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {
    private ModItems() {}

    public static Item RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED, RAINBOW_BANNER;

    private static Identifier id(String path) { return Identifier.of(TheOldWays.MOD_ID, path); }
    private static RegistryKey<Item> key(String path) { return RegistryKey.of(RegistryKeys.ITEM, id(path)); }

    public static void register() {
        // Ensure ModBlocks.register() has already run!

        // rainbow_wool item
        RAINBOW_WOOL = Registry.register(
                Registries.ITEM, id("rainbow_wool"),
                new BlockItem(
                        ModBlocks.RAINBOW_WOOL,
                        new Item.Settings().registryKey(key("rainbow_wool"))
                )
        );

        // rainbow_carpet item
        RAINBOW_CARPET = Registry.register(
                Registries.ITEM, id("rainbow_carpet"),
                new BlockItem(
                        ModBlocks.RAINBOW_CARPET,
                        new Item.Settings().registryKey(key("rainbow_carpet"))
                )
        );

        // rainbow_bed item (stack size 1)
        RAINBOW_BED = Registry.register(
                Registries.ITEM, id("rainbow_bed"),
                new BlockItem(
                        ModBlocks.RAINBOW_BED,
                        new Item.Settings().maxCount(1).registryKey(key("rainbow_bed"))
                )
        );

        // rainbow_banner item (places standing/wall automatically)
        RAINBOW_BANNER = Registry.register(
                Registries.ITEM, id("rainbow_banner"),
                new BannerItem(
                        ModBlocks.RAINBOW_BANNER,
                        ModBlocks.RAINBOW_WALL_BANNER,
                        new Item.Settings().maxCount(16).registryKey(key("rainbow_banner"))
                )
        );
    }
}
