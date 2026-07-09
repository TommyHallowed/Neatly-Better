package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;

import static net.hallowed.neatlybetter.init.ModBlocks.*;

public final class ModItemGroupRegistrar {
    private ModItemGroupRegistrar() {}

    public static void register() {

        NTRegistry.addToGroup(CreativeModeTabs.TOOLS_AND_UTILITIES, entries -> {
                entries.insertAfter(Items.MAP , ModItems.MAP_BUILDER);
                entries.insertAfter(Items.LEAD , ModItems.CHEST_KEY);
                entries.insertAfter(ModItems.CHEST_KEY , ModItems.QUIVER);
        });

        NTRegistry.addToGroup(CreativeModeTabs.FUNCTIONAL_BLOCKS, entries -> {
            entries.insertAfter(Items.COPPER_TORCH , GLOW_TORCH);
        });

        NTRegistry.addToGroup(CreativeModeTabs.FOOD_AND_DRINKS, entries -> {
            entries.insertAfter(Items.MILK_BUCKET , ModItems.MILK_BOTTLE);
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
            entries.insertAfter(Items.WOOL.pick(DyeColor.PINK) , RAINBOW_WOOL);
            entries.insertAfter(Items.CARPET.pick(DyeColor.PINK) , RAINBOW_CARPET);
            entries.insertAfter(Items.BED.pick(DyeColor.PINK) , RAINBOW_BED);
            entries.insertAfter(Items.TERRACOTTA , TERRACOTTA_STAIRS);
            entries.insertAfter(TERRACOTTA_STAIRS , TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.WHITE) , WHITE_TERRACOTTA_STAIRS);
            entries.insertAfter(WHITE_TERRACOTTA_STAIRS , WHITE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY) , LIGHT_GRAY_TERRACOTTA_STAIRS);
            entries.insertAfter(LIGHT_GRAY_TERRACOTTA_STAIRS , LIGHT_GRAY_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.GRAY) , GRAY_TERRACOTTA_STAIRS);
            entries.insertAfter(GRAY_TERRACOTTA_STAIRS , GRAY_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.BLACK) , BLACK_TERRACOTTA_STAIRS);
            entries.insertAfter(BLACK_TERRACOTTA_STAIRS , BLACK_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.BROWN) , BROWN_TERRACOTTA_STAIRS);
            entries.insertAfter(BROWN_TERRACOTTA_STAIRS , BROWN_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.RED) , RED_TERRACOTTA_STAIRS);
            entries.insertAfter(RED_TERRACOTTA_STAIRS , RED_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.ORANGE) , ORANGE_TERRACOTTA_STAIRS);
            entries.insertAfter(ORANGE_TERRACOTTA_STAIRS , ORANGE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.YELLOW) , YELLOW_TERRACOTTA_STAIRS);
            entries.insertAfter(YELLOW_TERRACOTTA_STAIRS , YELLOW_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.LIME) , LIME_TERRACOTTA_STAIRS);
            entries.insertAfter(LIME_TERRACOTTA_STAIRS , LIME_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.GREEN) , GREEN_TERRACOTTA_STAIRS);
            entries.insertAfter(GREEN_TERRACOTTA_STAIRS , GREEN_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.CYAN) , CYAN_TERRACOTTA_STAIRS);
            entries.insertAfter(CYAN_TERRACOTTA_STAIRS , CYAN_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE) , LIGHT_BLUE_TERRACOTTA_STAIRS);
            entries.insertAfter(LIGHT_BLUE_TERRACOTTA_STAIRS , LIGHT_BLUE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.BLUE) , BLUE_TERRACOTTA_STAIRS);
            entries.insertAfter(BLUE_TERRACOTTA_STAIRS , BLUE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.PURPLE) , PURPLE_TERRACOTTA_STAIRS);
            entries.insertAfter(PURPLE_TERRACOTTA_STAIRS , PURPLE_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.MAGENTA) , MAGENTA_TERRACOTTA_STAIRS);
            entries.insertAfter(MAGENTA_TERRACOTTA_STAIRS , MAGENTA_TERRACOTTA_SLAB);
            entries.insertAfter(Items.DYED_TERRACOTTA.pick(DyeColor.PINK) , PINK_TERRACOTTA_STAIRS);
            entries.insertAfter(PINK_TERRACOTTA_STAIRS , PINK_TERRACOTTA_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.WHITE) , WHITE_CONCRETE_STAIRS);
            entries.insertAfter(WHITE_CONCRETE_STAIRS , WHITE_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.LIGHT_GRAY) , LIGHT_GRAY_CONCRETE_STAIRS);
            entries.insertAfter(LIGHT_GRAY_CONCRETE_STAIRS , LIGHT_GRAY_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.GRAY) , GRAY_CONCRETE_STAIRS);
            entries.insertAfter(GRAY_CONCRETE_STAIRS , GRAY_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.BLACK) , BLACK_CONCRETE_STAIRS);
            entries.insertAfter(BLACK_CONCRETE_STAIRS , BLACK_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.BROWN) , BROWN_CONCRETE_STAIRS);
            entries.insertAfter(BROWN_CONCRETE_STAIRS , BROWN_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.RED) , RED_CONCRETE_STAIRS);
            entries.insertAfter(RED_CONCRETE_STAIRS , RED_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.ORANGE) , ORANGE_CONCRETE_STAIRS);
            entries.insertAfter(ORANGE_CONCRETE_STAIRS , ORANGE_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.YELLOW) , YELLOW_CONCRETE_STAIRS);
            entries.insertAfter(YELLOW_CONCRETE_STAIRS , YELLOW_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.LIME) , LIME_CONCRETE_STAIRS);
            entries.insertAfter(LIME_CONCRETE_STAIRS , LIME_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.GREEN) , GREEN_CONCRETE_STAIRS);
            entries.insertAfter(GREEN_CONCRETE_STAIRS , GREEN_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.CYAN) , CYAN_CONCRETE_STAIRS);
            entries.insertAfter(CYAN_CONCRETE_STAIRS , CYAN_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.LIGHT_BLUE) , LIGHT_BLUE_CONCRETE_STAIRS);
            entries.insertAfter(LIGHT_BLUE_CONCRETE_STAIRS , LIGHT_BLUE_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.BLUE) , BLUE_CONCRETE_STAIRS);
            entries.insertAfter(BLUE_CONCRETE_STAIRS , BLUE_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.PURPLE) , PURPLE_CONCRETE_STAIRS);
            entries.insertAfter(PURPLE_CONCRETE_STAIRS , PURPLE_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.MAGENTA) , MAGENTA_CONCRETE_STAIRS);
            entries.insertAfter(MAGENTA_CONCRETE_STAIRS , MAGENTA_CONCRETE_SLAB);
            entries.insertAfter(Items.CONCRETE.pick(DyeColor.PINK) , PINK_CONCRETE_STAIRS);
            entries.insertAfter(PINK_CONCRETE_STAIRS , PINK_CONCRETE_SLAB);
        });
    }
}
