package net.hallowed;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.hallowed.oldways.content.ModBlocks;
import net.hallowed.oldways.content.ModItems;
import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.mending.MendingNerf;
import net.hallowed.oldways.network.NetworkInit;
import net.hallowed.oldways.network.ServerHandlers;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheOldWays implements ModInitializer {
	public static final String MOD_ID = "old-ways";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        CommonConfigManager.load();
        MendingNerf.init();
        NetworkInit.register();
        ServerHandlers.register();

        // Add to creative tabs *after* registration (if you use Fabric API)
         ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
             entries.add(ModItems.RAINBOW_WOOL);
             entries.add(ModItems.RAINBOW_CARPET);
             entries.add(ModItems.RAINBOW_BED);
             entries.add(ModItems.RAINBOW_BANNER);
         });
		LOGGER.info("The Old Ways Mod Loaded!");
	}
}