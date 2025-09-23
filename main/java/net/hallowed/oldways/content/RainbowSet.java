// src/main/java/net/hallowed/oldways/content/RainbowSet.java
package net.hallowed.oldways.content;

import net.minecraft.block.*;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;

import static net.hallowed.oldways.content.ContentRegistry.id;

public final class RainbowSet {
    private RainbowSet() {}

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED, RAINBOW_BANNER, RAINBOW_WALL_BANNER;
    public static Item  RAINBOW_WOOL_ITEM, RAINBOW_CARPET_ITEM, RAINBOW_BED_ITEM, RAINBOW_BANNER_ITEM;

    // call these one-by-one to bisect
    public static void registerWool() {
        RAINBOW_WOOL = Registry.register(Registries.BLOCK, id("rainbow_wool"),
                new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL)));
        RAINBOW_WOOL_ITEM = Registry.register(Registries.ITEM, id("rainbow_wool"),
                new BlockItem(RAINBOW_WOOL, new Item.Settings()));
    }

    public static void registerCarpet() {
        RAINBOW_CARPET = Registry.register(Registries.BLOCK, id("rainbow_carpet"),
                new CarpetBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CARPET)));
        RAINBOW_CARPET_ITEM = Registry.register(Registries.ITEM, id("rainbow_carpet"),
                new BlockItem(RAINBOW_CARPET, new Item.Settings()));
    }

    public static void registerBed() {
        RAINBOW_BED = Registry.register(Registries.BLOCK, id("rainbow_bed"),
                new BedBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_BED)));
        RAINBOW_BED_ITEM = Registry.register(Registries.ITEM, id("rainbow_bed"),
                new BlockItem(RAINBOW_BED, new Item.Settings().maxCount(1)));
    }

    public static void registerBanner() {
        RAINBOW_BANNER = Registry.register(Registries.BLOCK, id("rainbow_banner"),
                new BannerBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_BANNER)));
        RAINBOW_WALL_BANNER = Registry.register(Registries.BLOCK, id("rainbow_wall_banner"),
                new WallBannerBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_WALL_BANNER)));
        RAINBOW_BANNER_ITEM = Registry.register(Registries.ITEM, id("rainbow_banner"),
                new BannerItem(RAINBOW_BANNER, RAINBOW_WALL_BANNER, new Item.Settings().maxCount(16)));
    }

    // original one-shot (call only after you confirm all four are safe)
    public static void register() {
        registerWool();
        registerCarpet();
        registerBed();
        registerBanner();
    }
}
