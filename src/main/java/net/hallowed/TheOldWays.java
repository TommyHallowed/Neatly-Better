package net.hallowed;

import net.fabricmc.api.ModInitializer;
import net.hallowed.oldways.api.OWBlockEntityTypeSupport;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.content.*;
import net.hallowed.oldways.content.feature.BoneMealExpansion;
import net.hallowed.oldways.enchantment.MendingNerf;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheOldWays implements ModInitializer {
    public static final String MOD_ID = "old-ways";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // 0) Common Config
        CommonConfigManager.load();

        // 1) Register content
        ModBlocks.register();
        ModItems.register();
        ModPotions.registerAll();
        ModBrewing.register();
        ModDataComponents.init();
        MendingNerf.init();
        BoneMealExpansion.init();

        // 2) Extend Bed block-entity support for custom bed
        OWBlockEntityTypeSupport.addSupported(BlockEntityType.BED, ModBlocks.RAINBOW_BED);

        // 3) Creative tab entries
        OWRegistry.addToGroup(ItemGroups.COLORED_BLOCKS, entries -> {
            entries.addAfter(Items.PINK_WOOL,   ModItems.RAINBOW_WOOL);
            entries.addAfter(Items.PINK_CARPET, ModItems.RAINBOW_CARPET);
            entries.addAfter(Items.PINK_BED,    ModItems.RAINBOW_BED);
            entries.addAfter(Items.PINK_BANNER, ModItems.RAINBOW_BANNER);
        });
        OWRegistry.flushItemGroups();

        // 4) Networking
        OldWaysNetwork.registerCommon();

        LOGGER.info("The Old Ways Mod Loaded!");
    }
}
