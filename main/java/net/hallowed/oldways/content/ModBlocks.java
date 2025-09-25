package net.hallowed.oldways.content;

import net.hallowed.TheOldWays;
import net.hallowed.oldways.content.block.RainbowBannerBlock;
import net.hallowed.oldways.content.block.RainbowWallBannerBlock;
import net.hallowed.oldways.mixin.accessor.BlockEntityTypeBlocksAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED;
    public static Block RAINBOW_BANNER, RAINBOW_WALL_BANNER;

    private static Identifier id(String path) { return Identifier.of(TheOldWays.MOD_ID, path); }
    private static RegistryKey<Block> key(String path) { return RegistryKey.of(RegistryKeys.BLOCK, id(path)); }

    public static void register() {
        // rainbow wool
        RAINBOW_WOOL = Registry.register(
                Registries.BLOCK, id("rainbow_wool"),
                new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).registryKey(key("rainbow_wool")))
        );

        // rainbow carpet
        RAINBOW_CARPET = Registry.register(
                Registries.BLOCK, id("rainbow_carpet"),
                new CarpetBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CARPET).registryKey(key("rainbow_carpet")))
        );

        // rainbow bed
        RAINBOW_BED = Registry.register(
                Registries.BLOCK, id("rainbow_bed"),
                new BedBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_BED).registryKey(key("rainbow_bed")))
        );

        // rainbow standing banner (uses our subclass but behaves like vanilla banner)
        RAINBOW_BANNER = Registry.register(
                Registries.BLOCK, id("rainbow_banner"),
                new RainbowBannerBlock(
                        AbstractBlock.Settings.copy(Blocks.WHITE_BANNER).registryKey(key("rainbow_banner"))
                )
        );

        // rainbow wall banner
        RAINBOW_WALL_BANNER = Registry.register(
                Registries.BLOCK, id("rainbow_wall_banner"),
                new RainbowWallBannerBlock(
                        AbstractBlock.Settings.copy(Blocks.WHITE_WALL_BANNER).registryKey(key("rainbow_wall_banner"))
                )
        );

        // Allow vanilla BannerBlockEntity to back our two banner blocks
        BlockEntityTypeBlocksAccessor acc = (BlockEntityTypeBlocksAccessor) BlockEntityType.BANNER;
        Set<Block> supported = acc.oldways$getBlocks();
        Set<Block> updated = new LinkedHashSet<>(supported);
        updated.add(RAINBOW_BANNER);
        updated.add(RAINBOW_WALL_BANNER);
        acc.oldways$setBlocks(updated);
    }
}
