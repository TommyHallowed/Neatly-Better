package net.hallowed;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;

import net.hallowed.neatlybetter.config.NTCommonConfig;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.feature.HostileAttributeTweaks;
import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.api.NTRegistry;
import net.hallowed.neatlybetter.init.*;
import net.hallowed.neatlybetter.network.NTNetwork;
import net.hallowed.neatlybetter.util.FastChunkScanner;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.fml.config.ModConfig;

public class NeatlyBetter implements ModInitializer {
    public static final String MOD_ID = "neatly-better";
    public static final BooleanProperty SHEARED = BooleanProperty.create("sheared");
    public static final BooleanProperty GLUED = BooleanProperty.create("glued");

    @Override
    public void onInitialize() {

        // 1) Server Config
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, NTServerConfig.CONFIG_SPEC);
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, NTCommonConfig.CONFIG_SPEC);

        // 2) Register content
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModAiGoals.register();
        ModPotions.registerAll();
        ModBrewing.register();
        ModTickets.init();
        ModEvents.init();
        ModData.init();
        HostileAttributeTweaks.init();

        // 3) Server Tick Events
        ServerLifecycleEvents.SERVER_STARTED.register(FastChunkScanner::restoreAllStructuresIcons);
        ServerTickEvents.END_SERVER_TICK.register(_ -> MapBuilderItem.MapGenerationQueue.tick());

        // 4) Creative tab entries
        ModItemGroupRegistrar.register();
        NTRegistry.flushItemGroups();

        // 5) Networking
        NTNetwork.registerCommon();

        // 6) Built-in Datapacks
        FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .ifPresent(container -> ResourceLoader.registerBuiltinPack(
                        Identifier.fromNamespaceAndPath(MOD_ID, "rebalance_datapack"),
                        container,
                        Component.literal("Neatly Better Rebalance"),
                        PackActivationType.DEFAULT_ENABLED
                ));
    }
}
