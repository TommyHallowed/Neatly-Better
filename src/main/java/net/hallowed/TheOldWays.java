package net.hallowed;

import net.fabricmc.api.ModInitializer;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.feature.BoneMealExpansion;
import net.hallowed.oldways.util.MendingNerf;
import net.hallowed.oldways.init.*;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.hallowed.oldways.init.ModItems.CRIMSON_BOAT;
import static net.hallowed.oldways.init.ModItems.WARPED_BOAT;

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
        ModDataComponents.init();
        MendingNerf.init();
        BoneMealExpansion.init();
        ModGameRules.register();


        // 2) Creative tab entries
        OWRegistry.addToGroup(ItemGroups.COLORED_BLOCKS, entries -> {
            entries.addAfter(Items.PINK_WOOL,   ModItems.RAINBOW_WOOL);
            entries.addAfter(Items.PINK_CARPET, ModItems.RAINBOW_CARPET);
            entries.addAfter(Items.PINK_BED,    ModItems.RAINBOW_BED);
            entries.addAfter(Items.PINK_BANNER, ModItems.RAINBOW_BANNER);
        });
        OWRegistry.addToGroup(ItemGroups.TOOLS, entries -> {
            entries.addAfter(Items.PALE_OAK_CHEST_BOAT, WARPED_BOAT);
            entries.addAfter(WARPED_BOAT, CRIMSON_BOAT);

        });

        OWRegistry.flushItemGroups();

        // 3) Networking
        OldWaysNetwork.registerCommon();

        LOGGER.info("The Old Ways Mod Loaded!");
    }
}
