package net.hallowed.neatlybetter;

import net.hallowed.neatlybetter.feature.cauldron.ModCauldronInteractions;
import net.hallowed.neatlybetter.registry.ModAiGoals;
import net.hallowed.neatlybetter.registry.ModBlockEntities;
import net.hallowed.neatlybetter.registry.ModBlocks;
import net.hallowed.neatlybetter.registry.ModData;
import net.hallowed.neatlybetter.registry.ModEvents;
import net.hallowed.neatlybetter.registry.ModItemGroupRegistrar;
import net.hallowed.neatlybetter.registry.ModItems;
import net.hallowed.neatlybetter.registry.ModPotions;
import net.hallowed.neatlybetter.registry.ModTickets;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;

import net.hallowed.neatlybetter.config.NTCommonConfig;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.feature.mobs.HostileAttributeTweaks;
import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.registry.NTRegistry;
import net.hallowed.neatlybetter.network.NTNetwork;
import net.hallowed.neatlybetter.feature.map.FastChunkScanner;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.hallowed.neatlybetter.mixin.block.BlockStateBaseAccessor;

import java.util.List;
import net.neoforged.fml.config.ModConfig;

public class NeatlyBetter implements ModInitializer {
    public static final String MOD_ID = "neatly-better";
    public static final BooleanProperty SHEARED = BooleanProperty.create("sheared");
    public static final BooleanProperty GLUED = BooleanProperty.create("glued");
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");

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
        ModCauldronInteractions.bootstrap();
        ModTickets.init();
        ModEvents.init();
        ModData.init();
        HostileAttributeTweaks.init();

        // 3) Server Tick Events
        ServerLifecycleEvents.SERVER_STARTED.register(FastChunkScanner::restoreAllStructuresIcons);
        ServerTickEvents.END_SERVER_TICK.register(_ -> MapBuilderItem.MapGenerationQueue.tick());
        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> MapBuilderItem.MapGenerationQueue.cancelAll());

        // Torchflowers emit light. Set once on vanilla's cached per-state value instead of hooking getLightEmission.
        for (Block block : List.of(Blocks.TORCHFLOWER, Blocks.POTTED_TORCHFLOWER)) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                ((BlockStateBaseAccessor) state).neatlybetter$setLightEmission(5);
            }
        }

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
                        PackActivationType.NORMAL
                ));
    }
}
