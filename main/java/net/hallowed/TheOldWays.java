package net.hallowed;

import net.fabricmc.api.ModInitializer;

import net.hallowed.config.CommonConfigManager;
import net.hallowed.mending.MendingNerf;
import net.hallowed.network.NetworkInit;
import net.hallowed.network.ServerHandlers;
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
        CommonConfigManager.load();
        MendingNerf.init();
        NetworkInit.register();
        ServerHandlers.register();
		LOGGER.info("The Old Ways Mod Loaded!");
	}
}