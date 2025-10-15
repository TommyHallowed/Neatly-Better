package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.block.GlowTorchBlock;
import net.hallowed.oldways.content.block.GlowWallTorchBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, GLOW_TORCH, GLOW_WALL_TORCH;

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

        GLOW_TORCH = OWRegistry.registerBlock(
                "glow_torch",
                new GlowTorchBlock(AbstractBlock.Settings
                        .copy(Blocks.TORCH)
                        .luminance(s -> 15)
                        .registryKey(key("glow_torch")))
        );

        GLOW_WALL_TORCH = OWRegistry.registerBlock(
                "glow_wall_torch",
                new GlowWallTorchBlock(AbstractBlock.Settings
                        .copy(Blocks.WALL_TORCH)
                        .luminance(s -> 15)
                        .registryKey(key("glow_wall_torch")))
        );
    }
}
