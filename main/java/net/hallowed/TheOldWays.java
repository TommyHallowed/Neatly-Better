package net.hallowed;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.hallowed.oldways.content.ModBlocks;
import net.hallowed.oldways.content.ModItems;
import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.mending.MendingNerf;
import net.hallowed.oldways.mixin.BlockEntityTypeBlocksAccessor;   // <-- our accessor
import net.hallowed.oldways.network.NetworkInit;
import net.hallowed.oldways.network.ServerHandlers;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TheOldWays implements ModInitializer {
    public static final String MOD_ID = "old-ways";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register blocks then items (must be in this order)
        ModBlocks.register();
        ModItems.register();

        // Add our custom blocks to vanilla BlockEntityType allow-lists
        addSupported(BlockEntityType.BED, ModBlocks.RAINBOW_BED);

        // Rest of your init
        CommonConfigManager.load();
        MendingNerf.init();
        NetworkInit.register();
        ServerHandlers.register();

        // Put items on creative tabs AFTER registration
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModItems.RAINBOW_WOOL);
            entries.add(ModItems.RAINBOW_CARPET);
            entries.add(ModItems.RAINBOW_BED);
        });

        LOGGER.info("The Old Ways Mod Loaded!");
    }

    /** Adds blocks to the private supported-blocks set on a BlockEntityType (bed/banner need this). */
    private static void addSupported(BlockEntityType<?> type, Block... toAdd) {
        BlockEntityTypeBlocksAccessor acc = (BlockEntityTypeBlocksAccessor) type;
        Set<Block> copy = new HashSet<>(acc.oldways$getBlocks()); // make mutable copy
        Collections.addAll(copy, toAdd);
        acc.oldways$setBlocks(copy); // write back
    }

}
