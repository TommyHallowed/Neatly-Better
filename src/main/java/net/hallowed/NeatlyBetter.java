package net.hallowed;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.feature.HostileAttributeTweaks;
import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.api.NTRegistry;
import net.hallowed.neatlybetter.init.*;
import net.hallowed.neatlybetter.network.NTNetwork;
import net.hallowed.neatlybetter.util.FastChunkScanner;

import net.neoforged.fml.config.ModConfig;

public class NeatlyBetter implements ModInitializer {
    public static final String MOD_ID = "neatly-better";

    @Override
    public void onInitialize() {

        // 1) Server Config
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, NTServerConfig.CONFIG_SPEC);

        // 2) Register content
        ModBlocks.register();
        ModAiGoals.register();
        ModItems.register();
        ModPotions.registerAll();
        ModBrewing.register();
        ModTickets.init();
        ModEvents.init();
        ModDataComponents.init();
        HostileAttributeTweaks.init();

        // 3) Server Tick Events
        ServerLifecycleEvents.SERVER_STARTED.register(FastChunkScanner::restoreAllStructuresIcons);
        ServerTickEvents.END_SERVER_TICK.register(server -> MapBuilderItem.MapGenerationQueue.tick());
        ServerTickEvents.END_SERVER_TICK.register(NTNetwork::flushDirtyBackpacks);

        // 4) Creative tab entries
        ModItemGroupRegistrar.register();
        NTRegistry.flushItemGroups();

        // 5) Networking
        NTNetwork.registerCommon();
    }
}
