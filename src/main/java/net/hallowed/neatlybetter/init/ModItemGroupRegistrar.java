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
        });

        NTRegistry.addToGroup(CreativeModeTabs.COLORED_BLOCKS, entries -> {
            entries.insertAfter(Items.PINK_WOOL , RAINBOW_WOOL);
            entries.insertAfter(Items.PINK_CARPET , RAINBOW_CARPET);
            entries.insertAfter(Items.PINK_BED , RAINBOW_BED);
        });
    }
}
