package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

import static net.hallowed.neatlybetter.init.ModBlocks.*;

public final class ModItemGroupRegistrar {
    private ModItemGroupRegistrar() {}

    public static void register() {

        NTRegistry.addToGroup(CreativeModeTabs.TOOLS_AND_UTILITIES, entries -> {
                entries.addAfter(Items.MAP , ModItems.MAP_BUILDER);
                entries.addAfter(Items.LEAD , ModItems.CHEST_KEY);
        });

        NTRegistry.addToGroup(CreativeModeTabs.BUILDING_BLOCKS, entries -> {
            entries.addAfter(Items.SMOOTH_STONE,   SMOOTH_STONE_STAIRS);
            entries.addAfter(Items.AMETHYST_BLOCK,   CALCITE_STAIRS);
            entries.addAfter(Items.QUARTZ_BRICKS,   QUARTZ_BRICK_STAIRS);
            entries.addAfter(Items.END_STONE,   END_STONE_STAIRS);
            entries.addAfter(Items.SMOOTH_BASALT,   SMOOTH_BASALT_STAIRS);
            entries.addAfter(CALCITE_STAIRS,   CALCITE_SLAB);
            entries.addAfter(QUARTZ_BRICK_STAIRS,   QUARTZ_BRICK_SLAB);
            entries.addAfter(END_STONE_STAIRS,   END_STONE_SLAB);
            entries.addAfter(SMOOTH_BASALT_STAIRS,   SMOOTH_BASALT_SLAB);
            entries.addAfter(Items.POLISHED_GRANITE_SLAB,   POLISHED_GRANITE_WALL);
            entries.addAfter(Items.POLISHED_ANDESITE_SLAB,   POLISHED_ANDESITE_WALL);
            entries.addAfter(Items.POLISHED_DIORITE_SLAB,   POLISHED_DIORITE_WALL);
            entries.addAfter(Items.STONE_SLAB,   STONE_WALL);
            entries.addAfter(Items.SMOOTH_STONE_SLAB,   SMOOTH_STONE_WALL);
            entries.addAfter(CALCITE_SLAB,   CALCITE_WALL);
            entries.addAfter(Items.QUARTZ_SLAB,   QUARTZ_WALL);
            entries.addAfter(QUARTZ_BRICK_SLAB,   QUARTZ_BRICK_WALL);
            entries.addAfter(Items.SMOOTH_QUARTZ_SLAB,   SMOOTH_QUARTZ_WALL);
            entries.addAfter(END_STONE_SLAB,   END_STONE_WALL);
            entries.addAfter(Items.PURPUR_SLAB,   PURPUR_WALL);
            entries.addAfter(SMOOTH_BASALT_SLAB,   SMOOTH_BASALT_WALL);
            entries.addAfter(Items.PRISMARINE_BRICK_SLAB,   PRISMARINE_BRICK_WALL);
            entries.addAfter(Items.DARK_PRISMARINE_SLAB,   DARK_PRISMARINE_WALL);
            entries.addAfter(Items.SMOOTH_SANDSTONE_SLAB,   SMOOTH_SANDSTONE_WALL);
            entries.addAfter(Items.SMOOTH_RED_SANDSTONE_SLAB,   SMOOTH_RED_SANDSTONE_WALL);
        });

        NTRegistry.addToGroup(CreativeModeTabs.COLORED_BLOCKS, entries -> {
            entries.addAfter(Items.PINK_WOOL , RAINBOW_WOOL);
            entries.addAfter(Items.PINK_CARPET , RAINBOW_CARPET);
        });
    }
}
