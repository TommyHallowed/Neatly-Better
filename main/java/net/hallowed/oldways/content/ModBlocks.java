package net.hallowed.oldways.content;

import net.hallowed.TheOldWays;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED;

    private static Identifier id(String path) { return Identifier.of(TheOldWays.MOD_ID, path); }
    private static RegistryKey<Block> key(String path) { return RegistryKey.of(RegistryKeys.BLOCK, id(path)); }

    public static void register() {
        // RAINBOW_WOOL
        Identifier woolId = id("rainbow_wool");
        RAINBOW_WOOL = Registry.register(
                Registries.BLOCK, woolId,
                new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).registryKey(key("rainbow_wool")))
        );

        // RAINBOW_CARPET
        Identifier carpetId = id("rainbow_carpet");
        RAINBOW_CARPET = Registry.register(
                Registries.BLOCK, carpetId,
                new CarpetBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CARPET).registryKey(key("rainbow_carpet")))
        );

        // RAINBOW_BED (note: BedBlock still needs DyeColor)
        Identifier bedId = id("rainbow_bed");
        RAINBOW_BED = Registry.register(
                Registries.BLOCK, bedId,
                new BedBlock(DyeColor.WHITE, AbstractBlock.Settings.copy(Blocks.WHITE_BED).registryKey(key("rainbow_bed")))
        );

    }
}
