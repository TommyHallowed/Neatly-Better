package net.hallowed.oldways.content;

import net.hallowed.oldways.api.OWRegistry;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

/** Registers item forms for your rainbow blocks. */
public final class ModItems {
    private ModItems() {}

    public static Item RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED, RAINBOW_BANNER;

    private static RegistryKey<Item> key(String path) {
        return RegistryKey.of(RegistryKeys.ITEM, OWRegistry.id(path));
    }

    public static void register() {
        // rainbow_wool item
        RAINBOW_WOOL = OWRegistry.registerItem(
                "rainbow_wool",
                new BlockItem(
                        ModBlocks.RAINBOW_WOOL,
                        new Item.Settings().registryKey(key("rainbow_wool"))
                )
        );

        // rainbow_carpet item
        RAINBOW_CARPET = OWRegistry.registerItem(
                "rainbow_carpet",
                new BlockItem(
                        ModBlocks.RAINBOW_CARPET,
                        new Item.Settings().registryKey(key("rainbow_carpet"))
                )
        );

        // rainbow_bed item
        RAINBOW_BED = OWRegistry.registerItem(
                "rainbow_bed",
                new BlockItem(
                        ModBlocks.RAINBOW_BED,
                        new Item.Settings().maxCount(1).registryKey(key("rainbow_bed"))
                )
        );

        // rainbow_banner item (standing + wall variants)
        RAINBOW_BANNER = OWRegistry.registerItem(
                "rainbow_banner",
                new BannerItem(
                        ModBlocks.RAINBOW_BANNER,
                        ModBlocks.RAINBOW_WALL_BANNER,
                        new Item.Settings().maxCount(16).registryKey(key("rainbow_banner"))
                )
        );
    }
}
