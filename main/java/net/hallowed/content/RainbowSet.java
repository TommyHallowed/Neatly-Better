package net.hallowed.content;

import net.minecraft.block.*;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;

import static net.hallowed.content.ContentRegistry.blockWithItem;
import static net.hallowed.content.ContentRegistry.id;
import static net.hallowed.content.ContentRegistry.item;

public final class RainbowSet {
    private RainbowSet() {}

    // Blocks
    public static Block RAINBOW_WOOL;
    public static Block RAINBOW_CARPET;
    public static Block RAINBOW_BED;
    public static Block RAINBOW_BANNER;      // standing
    public static Block RAINBOW_WALL_BANNER; // wall

    // Items (BlockItems are auto-registered by blockWithItem; we fetch refs for convenience)
    public static Item RAINBOW_WOOL_ITEM;
    public static Item RAINBOW_CARPET_ITEM;
    public static Item RAINBOW_BED_ITEM;
    public static Item RAINBOW_BANNER_ITEM; // BannerItem is special

    public static void register() {
        // rainbow_wool: clone of white_wool (texture replaced by our assets/RP)
        RAINBOW_WOOL = blockWithItem("rainbow_wool",
                new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL)),
                new Item.Settings());
        RAINBOW_WOOL_ITEM = Registries.ITEM.get(id("rainbow_wool"));

        // rainbow_carpet: clone of white_carpet
        RAINBOW_CARPET = blockWithItem("rainbow_carpet",
                new CarpetBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CARPET)),
                new Item.Settings());
        RAINBOW_CARPET_ITEM = Registries.ITEM.get(id("rainbow_carpet"));

        // rainbow_bed: clone of white_bed
        RAINBOW_BED = blockWithItem("rainbow_bed",
                new BedBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_BED)),
                new Item.Settings().maxCount(1));
        RAINBOW_BED_ITEM = Registries.ITEM.get(id("rainbow_bed"));

        // rainbow_banner: standing + wall blocks
        RAINBOW_BANNER = Registry.register(Registries.BLOCK, id("rainbow_banner"),
                new BannerBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_BANNER)));
        RAINBOW_WALL_BANNER = Registry.register(Registries.BLOCK, id("rainbow_wall_banner"),
                new WallBannerBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_WALL_BANNER)));

        // Banner item linking both blocks; patterns behave like vanilla
        RAINBOW_BANNER_ITEM = item("rainbow_banner",
                new BannerItem( RAINBOW_BANNER, RAINBOW_WALL_BANNER, new Item.Settings().maxCount(16)));
    }
}
