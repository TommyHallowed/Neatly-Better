package net.hallowed;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.hallowed.neatlybetter.content.feature.HostileAttributeTweaks;
import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.api.NTRegistry;
import net.hallowed.neatlybetter.init.*;
import net.hallowed.neatlybetter.network.NTNetwork;
import net.hallowed.neatlybetter.util.FastChunkScanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeatlyBetter implements ModInitializer {
    public static final String MOD_ID = "neatly-better";
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

        // 2) Server Tick Events
        ServerLifecycleEvents.SERVER_STARTED.register(FastChunkScanner::restoreAllStructuresIcons);
        ServerTickEvents.END_SERVER_TICK.register(server -> MapBuilderItem.MapGenerationQueue.tick());
        ServerTickEvents.END_SERVER_TICK.register(NTNetwork::flushDirtyBackpacks);

        // 3) Creative tab entries
        ModItemGroupRegistrar.register();
        NTRegistry.flushItemGroups();

        // 4) Networking
        NTNetwork.registerCommon();

        LOGGER.info("Neatly Better Mod Loaded!");
    }
}
