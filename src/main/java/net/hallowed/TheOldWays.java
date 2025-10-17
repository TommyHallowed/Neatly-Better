package net.hallowed;

import net.fabricmc.api.ModInitializer;
import net.hallowed.oldways.init.ModEvents;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.init.*;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.hallowed.oldways.init.ModBlocks.*;
import static net.hallowed.oldways.init.ModItems.*;
import static net.hallowed.oldways.init.ModItems.GLOW_TORCH;

public class TheOldWays implements ModInitializer {
    public static final String MOD_ID = "old-ways";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        // 1) Register content
        ModBlocks.register();
        ModEntities.register();
        ModAiGoals.register();
        ModItems.register();
        ModPotions.registerAll();
        ModBrewing.register();
        ModGameRules.register();
        ModEvents.init();
        ModDataComponents.init();

        // 2) Creative tab entries
        OWRegistry.addToGroup(ItemGroups.COLORED_BLOCKS, entries -> {
            entries.addAfter(Items.PINK_WOOL,   ModBlocks.RAINBOW_WOOL);
            entries.addAfter(Items.PINK_CARPET, ModBlocks.RAINBOW_CARPET);
        });
        OWRegistry.addToGroup(ItemGroups.TOOLS, entries -> {
            entries.addAfter(Items.PALE_OAK_CHEST_BOAT, WARPED_BOAT);
            entries.addAfter(WARPED_BOAT, CRIMSON_BOAT);
            entries.addAfter(Items.FIREWORK_ROCKET, DRAGON_BURST_ROCKET);
        });
        OWRegistry.addToGroup(ItemGroups.FUNCTIONAL, entries ->
                entries.addAfter(Items.COPPER_TORCH, GLOW_TORCH));

        OWRegistry.addToGroup(ItemGroups.COMBAT, entries ->
                entries.addAfter(Items.DIAMOND_HORSE_ARMOR, NETHERITE_HORSE_ARMOR));

        OWRegistry.addToGroup(ItemGroups.BUILDING_BLOCKS, entries -> {
            entries.addAfter(Items.SMOOTH_STONE,   SMOOTH_STONE_STAIRS);
            entries.addAfter(Items.CALCITE,   CALCITE_STAIRS);
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
        });

        OWRegistry.flushItemGroups();

        // 3) Networking
        OldWaysNetwork.registerCommon();

        LOGGER.info("The Old Ways Mod Loaded!");
    }
}
