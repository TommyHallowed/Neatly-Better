package net.hallowed;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hallowed.oldways.content.feature.HostileAttributeTweaks;
import net.hallowed.oldways.content.item.MapBuilderItem;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.init.*;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.hallowed.oldways.util.FastChunkScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheOldWays implements ModInitializer {
    public static final String MOD_ID = "old-ways";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        // 1) Register content
        ModBlocks.register();
        ModAiGoals.register();
        ModItems.register();
        ModPotions.registerAll();
        ModBrewing.register();
        ModGameRules.register();
        ModTickets.init();
        ModEvents.init();
        ModDataComponents.init();
        HostileAttributeTweaks.init();
        ServerLifecycleEvents.SERVER_STARTED.register(FastChunkScanner::restoreAllStructures);
        ServerTickEvents.END_SERVER_TICK.register(server -> MapBuilderItem.MapGenerationQueue.tick());

        // Flush dirty backpack state -- only sends packets for players whose backpack slots changed
        ServerTickEvents.END_SERVER_TICK.register(OldWaysNetwork::flushDirtyBackpacks);

        // 2) Creative tab entries
        ModItemGroupRegistrar.register();
        OWRegistry.flushItemGroups();

        // 3) Networking
        OldWaysNetwork.registerCommon();

        LOGGER.info("The Old Ways Mod Loaded!");
    }
}
