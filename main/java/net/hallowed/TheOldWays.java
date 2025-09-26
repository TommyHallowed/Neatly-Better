package net.hallowed;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.hallowed.oldways.content.*;
import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.mixin.accessor.BlockEntityTypeBlocksAccessor;   // <-- our accessor
import net.hallowed.oldways.network.NetworkInit;
import net.hallowed.oldways.network.ServerHandlers;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
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
        ModPotions.registerAll();
        ModBrewing.register();
        addSupported(ModBlocks.RAINBOW_BED);
        CommonConfigManager.load();
        NetworkInit.register();
        ServerHandlers.register();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(entries -> {
            entries.addAfter(Items.PINK_WOOL, ModItems.RAINBOW_WOOL);
            entries.addAfter(Items.PINK_CARPET, ModItems.RAINBOW_CARPET);
            entries.addAfter(Items.PINK_BED, ModItems.RAINBOW_BED);
            entries.addAfter(Items.PINK_BANNER, ModItems.RAINBOW_BANNER);
        });

        LOGGER.info("The Old Ways Mod Loaded!");
    }

    /** Adds blocks to the private supported-blocks set on a BlockEntityType (bed/banner need this). */
    private static void addSupported(Block... toAdd) {
        BlockEntityTypeBlocksAccessor acc = (BlockEntityTypeBlocksAccessor) BlockEntityType.BED;
        Set<Block> copy = new HashSet<>(acc.oldways$getBlocks()); // make mutable copy
        Collections.addAll(copy, toAdd);
        acc.oldways$setBlocks(copy); // write back
    }

}
