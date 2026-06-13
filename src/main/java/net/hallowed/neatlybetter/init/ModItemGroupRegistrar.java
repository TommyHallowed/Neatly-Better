package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

import static net.hallowed.neatlybetter.init.ModBlocks.*;

public final class ModItemGroupRegistrar {
    private ModItemGroupRegistrar() {}

    public static void register() {

        NTRegistry.addToGroup(CreativeModeTabs.TOOLS_AND_UTILITIES, entries -> {
                entries.insertAfter(Items.MAP , ModItems.MAP_BUILDER);
                entries.insertAfter(Items.LEAD , ModItems.CHEST_KEY);
            entries.insertAfter(ModItems.CHEST_KEY, ModItems.WOLF_COLLAR);
        });

        NTRegistry.addToGroup(CreativeModeTabs.BUILDING_BLOCKS, entries -> {
            entries.insertAfter(Items.SMOOTH_STONE,   SMOOTH_STONE_STAIRS);
            entries.insertAfter(Items.AMETHYST_BLOCK,   CALCITE_STAIRS);
            entries.insertAfter(Items.QUARTZ_BRICKS,   QUARTZ_BRICK_STAIRS);
            entries.insertAfter(Items.END_STONE,   END_STONE_STAIRS);
            entries.insertAfter(Items.SMOOTH_BASALT,   SMOOTH_BASALT_STAIRS);
            entries.insertAfter(CALCITE_STAIRS,   CALCITE_SLAB);
            entries.insertAfter(QUARTZ_BRICK_STAIRS,   QUARTZ_BRICK_SLAB);
            entries.insertAfter(END_STONE_STAIRS,   END_STONE_SLAB);
            entries.insertAfter(SMOOTH_BASALT_STAIRS,   SMOOTH_BASALT_SLAB);
            entries.insertAfter(Items.POLISHED_GRANITE_SLAB,   POLISHED_GRANITE_WALL);
            entries.insertAfter(Items.POLISHED_ANDESITE_SLAB,   POLISHED_ANDESITE_WALL);
            entries.insertAfter(Items.POLISHED_DIORITE_SLAB,   POLISHED_DIORITE_WALL);
            entries.insertAfter(Items.STONE_SLAB,   STONE_WALL);
            entries.insertAfter(Items.SMOOTH_STONE_SLAB,   SMOOTH_STONE_WALL);
            entries.insertAfter(CALCITE_SLAB,   CALCITE_WALL);
            entries.insertAfter(Items.QUARTZ_SLAB,   QUARTZ_WALL);
            entries.insertAfter(QUARTZ_BRICK_SLAB,   QUARTZ_BRICK_WALL);
            entries.insertAfter(Items.SMOOTH_QUARTZ_SLAB,   SMOOTH_QUARTZ_WALL);
            entries.insertAfter(END_STONE_SLAB,   END_STONE_WALL);
            entries.insertAfter(Items.PURPUR_SLAB,   PURPUR_WALL);
            entries.insertAfter(SMOOTH_BASALT_SLAB,   SMOOTH_BASALT_WALL);
            entries.insertAfter(Items.PRISMARINE_BRICK_SLAB,   PRISMARINE_BRICK_WALL);
            entries.insertAfter(Items.DARK_PRISMARINE_SLAB,   DARK_PRISMARINE_WALL);
            entries.insertAfter(Items.SMOOTH_SANDSTONE_SLAB,   SMOOTH_SANDSTONE_WALL);
            entries.insertAfter(Items.SMOOTH_RED_SANDSTONE_SLAB,   SMOOTH_RED_SANDSTONE_WALL);
            entries.insertAfter(Items.COAL_BLOCK,   CHARCOAL_BLOCK);
        });

        NTRegistry.addToGroup(CreativeModeTabs.COLORED_BLOCKS, entries -> {
            entries.insertAfter(Items.PINK_WOOL , RAINBOW_WOOL);
            entries.insertAfter(Items.PINK_CARPET , RAINBOW_CARPET);
            entries.insertAfter(Items.PINK_BED , RAINBOW_BED);
            entries.insertAfter(Items.TERRACOTTA , TERRACOTTA_STAIRS);
            entries.insertAfter(TERRACOTTA_STAIRS , TERRACOTTA_SLAB);
            entries.insertAfter(Items.WHITE_TERRACOTTA , WHITE_TERRACOTTA_STAIRS);
            entries.insertAfter(WHITE_TERRACOTTA_STAIRS , WHITE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.LIGHT_GRAY_TERRACOTTA , LIGHT_GRAY_TERRACOTTA_STAIRS);
            entries.insertAfter(LIGHT_GRAY_TERRACOTTA_STAIRS , LIGHT_GRAY_TERRACOTTA_SLAB);
            entries.insertAfter(Items.GRAY_TERRACOTTA , GRAY_TERRACOTTA_STAIRS);
            entries.insertAfter(GRAY_TERRACOTTA_STAIRS , GRAY_TERRACOTTA_SLAB);
            entries.insertAfter(Items.BLACK_TERRACOTTA , BLACK_TERRACOTTA_STAIRS);
            entries.insertAfter(BLACK_TERRACOTTA_STAIRS , BLACK_TERRACOTTA_SLAB);
            entries.insertAfter(Items.BROWN_TERRACOTTA , BROWN_TERRACOTTA_STAIRS);
            entries.insertAfter(BROWN_TERRACOTTA_STAIRS , BROWN_TERRACOTTA_SLAB);
            entries.insertAfter(Items.RED_TERRACOTTA , RED_TERRACOTTA_STAIRS);
            entries.insertAfter(RED_TERRACOTTA_STAIRS , RED_TERRACOTTA_SLAB);
            entries.insertAfter(Items.ORANGE_TERRACOTTA , ORANGE_TERRACOTTA_STAIRS);
            entries.insertAfter(ORANGE_TERRACOTTA_STAIRS , ORANGE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.YELLOW_TERRACOTTA , YELLOW_TERRACOTTA_STAIRS);
            entries.insertAfter(YELLOW_TERRACOTTA_STAIRS , YELLOW_TERRACOTTA_SLAB);
            entries.insertAfter(Items.LIME_TERRACOTTA , LIME_TERRACOTTA_STAIRS);
            entries.insertAfter(LIME_TERRACOTTA_STAIRS , LIME_TERRACOTTA_SLAB);
            entries.insertAfter(Items.GREEN_TERRACOTTA , GREEN_TERRACOTTA_STAIRS);
            entries.insertAfter(GREEN_TERRACOTTA_STAIRS , GREEN_TERRACOTTA_SLAB);
            entries.insertAfter(Items.CYAN_TERRACOTTA , CYAN_TERRACOTTA_STAIRS);
            entries.insertAfter(CYAN_TERRACOTTA_STAIRS , CYAN_TERRACOTTA_SLAB);
            entries.insertAfter(Items.LIGHT_BLUE_TERRACOTTA , LIGHT_BLUE_TERRACOTTA_STAIRS);
            entries.insertAfter(LIGHT_BLUE_TERRACOTTA_STAIRS , LIGHT_BLUE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.BLUE_TERRACOTTA , BLUE_TERRACOTTA_STAIRS);
            entries.insertAfter(BLUE_TERRACOTTA_STAIRS , BLUE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.PURPLE_TERRACOTTA , PURPLE_TERRACOTTA_STAIRS);
            entries.insertAfter(PURPLE_TERRACOTTA_STAIRS , PURPLE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.MAGENTA_TERRACOTTA , MAGENTA_TERRACOTTA_STAIRS);
            entries.insertAfter(MAGENTA_TERRACOTTA_STAIRS , MAGENTA_TERRACOTTA_SLAB);
            entries.insertAfter(Items.PINK_TERRACOTTA , PINK_TERRACOTTA_STAIRS);
            entries.insertAfter(PINK_TERRACOTTA_STAIRS , PINK_TERRACOTTA_SLAB);
            entries.insertAfter(Items.WHITE_CONCRETE , WHITE_CONCRETE_STAIRS);
            entries.insertAfter(WHITE_CONCRETE_STAIRS , WHITE_CONCRETE_SLAB);
            entries.insertAfter(Items.LIGHT_GRAY_CONCRETE , LIGHT_GRAY_CONCRETE_STAIRS);
            entries.insertAfter(LIGHT_GRAY_CONCRETE_STAIRS , LIGHT_GRAY_CONCRETE_SLAB);
            entries.insertAfter(Items.GRAY_CONCRETE , GRAY_CONCRETE_STAIRS);
            entries.insertAfter(GRAY_CONCRETE_STAIRS , GRAY_CONCRETE_SLAB);
            entries.insertAfter(Items.BLACK_CONCRETE , BLACK_CONCRETE_STAIRS);
            entries.insertAfter(BLACK_CONCRETE_STAIRS , BLACK_CONCRETE_SLAB);
            entries.insertAfter(Items.BROWN_CONCRETE , BROWN_CONCRETE_STAIRS);
            entries.insertAfter(BROWN_CONCRETE_STAIRS , BROWN_CONCRETE_SLAB);
            entries.insertAfter(Items.RED_CONCRETE , RED_CONCRETE_STAIRS);
            entries.insertAfter(RED_CONCRETE_STAIRS , RED_CONCRETE_SLAB);
            entries.insertAfter(Items.ORANGE_CONCRETE , ORANGE_CONCRETE_STAIRS);
            entries.insertAfter(ORANGE_CONCRETE_STAIRS , ORANGE_CONCRETE_SLAB);
            entries.insertAfter(Items.YELLOW_CONCRETE , YELLOW_CONCRETE_STAIRS);
            entries.insertAfter(YELLOW_CONCRETE_STAIRS , YELLOW_CONCRETE_SLAB);
            entries.insertAfter(Items.LIME_CONCRETE , LIME_CONCRETE_STAIRS);
            entries.insertAfter(LIME_CONCRETE_STAIRS , LIME_CONCRETE_SLAB);
            entries.insertAfter(Items.GREEN_CONCRETE , GREEN_CONCRETE_STAIRS);
            entries.insertAfter(GREEN_CONCRETE_STAIRS , GREEN_CONCRETE_SLAB);
            entries.insertAfter(Items.CYAN_CONCRETE , CYAN_CONCRETE_STAIRS);
            entries.insertAfter(CYAN_CONCRETE_STAIRS , CYAN_CONCRETE_SLAB);
            entries.insertAfter(Items.LIGHT_BLUE_CONCRETE , LIGHT_BLUE_CONCRETE_STAIRS);
            entries.insertAfter(LIGHT_BLUE_CONCRETE_STAIRS , LIGHT_BLUE_CONCRETE_SLAB);
            entries.insertAfter(Items.BLUE_CONCRETE , BLUE_CONCRETE_STAIRS);
            entries.insertAfter(BLUE_CONCRETE_STAIRS , BLUE_CONCRETE_SLAB);
            entries.insertAfter(Items.PURPLE_CONCRETE , PURPLE_CONCRETE_STAIRS);
            entries.insertAfter(PURPLE_CONCRETE_STAIRS , PURPLE_CONCRETE_SLAB);
            entries.insertAfter(Items.MAGENTA_CONCRETE , MAGENTA_CONCRETE_STAIRS);
            entries.insertAfter(MAGENTA_CONCRETE_STAIRS , MAGENTA_CONCRETE_SLAB);
            entries.insertAfter(Items.PINK_CONCRETE , PINK_CONCRETE_STAIRS);
            entries.insertAfter(PINK_CONCRETE_STAIRS , PINK_CONCRETE_SLAB);
        });
    }
}
