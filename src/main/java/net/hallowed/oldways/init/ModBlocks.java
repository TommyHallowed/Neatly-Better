package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWBlockEntityTypeSupport;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.block.RainbowBannerBlock;
import net.hallowed.oldways.content.block.RainbowWallBannerBlock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED, RAINBOW_BANNER, RAINBOW_WALL_BANNER;

    private static RegistryKey<Block> key(String path) {
        return RegistryKey.of(RegistryKeys.BLOCK, OWRegistry.id(path));
    }

    public static void register() {
        // rainbow wool
        RAINBOW_WOOL = OWRegistry.registerBlock(
                "rainbow_wool",
                new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).registryKey(key("rainbow_wool")))
        );

        // rainbow carpet
        RAINBOW_CARPET = OWRegistry.registerBlock(
                "rainbow_carpet",
                new CarpetBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CARPET).registryKey(key("rainbow_carpet")))
        );

        // rainbow bed
        RAINBOW_BED = OWRegistry.registerBlock(
                "rainbow_bed",
                new BedBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_BED).registryKey(key("rainbow_bed")))
        );

        // rainbow standing banner
        RAINBOW_BANNER = OWRegistry.registerBlock(
                "rainbow_banner",
                new RainbowBannerBlock(AbstractBlock.Settings.copy(Blocks.WHITE_BANNER).registryKey(key("rainbow_banner")))
        );

        // rainbow wall banner
        RAINBOW_WALL_BANNER = OWRegistry.registerBlock(
                "rainbow_wall_banner",
                new RainbowWallBannerBlock(AbstractBlock.Settings.copy(Blocks.WHITE_WALL_BANNER).registryKey(key("rainbow_wall_banner")))
        );

        // Allow vanilla BannerBlockEntity to back our two banner blocks (using helper)
        OWBlockEntityTypeSupport.addSupported(BlockEntityType.BANNER, RAINBOW_BANNER, RAINBOW_WALL_BANNER);
        OWBlockEntityTypeSupport.addSupported(BlockEntityType.BED, ModBlocks.RAINBOW_BED);
    }
}
