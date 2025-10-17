package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.block.*;
import net.minecraft.block.*;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, GLOW_TORCH, GLOW_WALL_TORCH;

    public static Block SMOOTH_STONE_STAIRS, CALCITE_STAIRS, QUARTZ_BRICK_STAIRS, END_STONE_STAIRS, SMOOTH_BASALT_STAIRS;

    public static Block CALCITE_SLAB, QUARTZ_BRICK_SLAB, END_STONE_SLAB, SMOOTH_BASALT_SLAB;

    public static Block POLISHED_GRANITE_WALL, POLISHED_ANDESITE_WALL, POLISHED_DIORITE_WALL, STONE_WALL,
            SMOOTH_STONE_WALL, CALCITE_WALL, QUARTZ_WALL, QUARTZ_BRICK_WALL, SMOOTH_QUARTZ_WALL,
            END_STONE_WALL, PURPUR_WALL, SMOOTH_BASALT_WALL, PRISMARINE_BRICK_WALL, DARK_PRISMARINE_WALL;

    public static void register() {
        // Blocks
        RAINBOW_WOOL = OWRegistry.registerBlockWithItem(
                "rainbow_wool",
                new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).registryKey(OWRegistry.blockKey("rainbow_wool")))
        );

        RAINBOW_CARPET = OWRegistry.registerBlockWithItem(
                "rainbow_carpet",
                new CarpetBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CARPET).registryKey(OWRegistry.blockKey("rainbow_carpet")))
        );

        GLOW_TORCH = OWRegistry.registerBlock("glow_torch",
                new GlowTorchBlock(AbstractBlock.Settings.copy(Blocks.TORCH).luminance(s -> 15).registryKey(OWRegistry.blockKey("glow_torch"))));

        GLOW_WALL_TORCH = OWRegistry.registerBlock("glow_wall_torch",
                new GlowWallTorchBlock(AbstractBlock.Settings.copy(Blocks.WALL_TORCH).luminance(s -> 15).registryKey(OWRegistry.blockKey("glow_wall_torch"))));

        // Stairs
        SMOOTH_STONE_STAIRS = OWRegistry.registerBlockWithItem(
                "smooth_stone_stairs",
                new StairsBlock(Blocks.SMOOTH_STONE.getDefaultState(),
                        AbstractBlock.Settings.copy(Blocks.SMOOTH_STONE).requiresTool().registryKey(OWRegistry.blockKey("smooth_stone_stairs")))
        );

        CALCITE_STAIRS = OWRegistry.registerBlockWithItem(
                "calcite_stairs",
                new StairsBlock(Blocks.CALCITE.getDefaultState(),
                        AbstractBlock.Settings.copy(Blocks.CALCITE).requiresTool().registryKey(OWRegistry.blockKey("calcite_stairs")))
        );

        QUARTZ_BRICK_STAIRS = OWRegistry.registerBlockWithItem(
                "quartz_brick_stairs",
                new StairsBlock(Blocks.QUARTZ_BRICKS.getDefaultState(),
                        AbstractBlock.Settings.copy(Blocks.QUARTZ_BRICKS).requiresTool().registryKey(OWRegistry.blockKey("quartz_brick_stairs")))
        );

        END_STONE_STAIRS = OWRegistry.registerBlockWithItem(
                "end_stone_stairs",
                new StairsBlock(Blocks.END_STONE.getDefaultState(),
                        AbstractBlock.Settings.copy(Blocks.END_STONE).requiresTool().registryKey(OWRegistry.blockKey("end_stone_stairs")))
        );

        SMOOTH_BASALT_STAIRS = OWRegistry.registerBlockWithItem(
                "smooth_basalt_stairs",
                new StairsBlock(Blocks.SMOOTH_BASALT.getDefaultState(),
                        AbstractBlock.Settings.copy(Blocks.SMOOTH_BASALT).requiresTool().registryKey(OWRegistry.blockKey("smooth_basalt_stairs")))
        );

        // Slabs
        CALCITE_SLAB = OWRegistry.registerBlockWithItem(
                "calcite_slab",
                new SlabBlock(AbstractBlock.Settings.copy(Blocks.CALCITE).requiresTool().registryKey(OWRegistry.blockKey("calcite_slab")))
        );

        QUARTZ_BRICK_SLAB = OWRegistry.registerBlockWithItem(
                "quartz_brick_slab",
                new SlabBlock(AbstractBlock.Settings.copy(Blocks.QUARTZ_BRICKS).requiresTool().registryKey(OWRegistry.blockKey("quartz_brick_slab")))
        );

        END_STONE_SLAB = OWRegistry.registerBlockWithItem(
                "end_stone_slab",
                new SlabBlock(AbstractBlock.Settings.copy(Blocks.END_STONE).requiresTool().registryKey(OWRegistry.blockKey("end_stone_slab")))
        );

        SMOOTH_BASALT_SLAB = OWRegistry.registerBlockWithItem(
                "smooth_basalt_slab",
                new SlabBlock(AbstractBlock.Settings.copy(Blocks.SMOOTH_BASALT).requiresTool().registryKey(OWRegistry.blockKey("smooth_basalt_slab")))
        );

        // Walls
        POLISHED_GRANITE_WALL = OWRegistry.registerBlockWithItem("polished_granite_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.POLISHED_GRANITE).requiresTool().registryKey(OWRegistry.blockKey("polished_granite_wall"))));

        POLISHED_ANDESITE_WALL = OWRegistry.registerBlockWithItem("polished_andesite_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.POLISHED_ANDESITE).requiresTool().registryKey(OWRegistry.blockKey("polished_andesite_wall"))));

        POLISHED_DIORITE_WALL = OWRegistry.registerBlockWithItem("polished_diorite_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.POLISHED_DIORITE).requiresTool().registryKey(OWRegistry.blockKey("polished_diorite_wall"))));

        STONE_WALL = OWRegistry.registerBlockWithItem("stone_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.STONE).requiresTool().registryKey(OWRegistry.blockKey("stone_wall"))));

        SMOOTH_STONE_WALL = OWRegistry.registerBlockWithItem("smooth_stone_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.SMOOTH_STONE).requiresTool().registryKey(OWRegistry.blockKey("smooth_stone_wall"))));

        CALCITE_WALL = OWRegistry.registerBlockWithItem("calcite_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.CALCITE).requiresTool().registryKey(OWRegistry.blockKey("calcite_wall"))));

        QUARTZ_WALL = OWRegistry.registerBlockWithItem("quartz_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.QUARTZ_BLOCK).requiresTool().registryKey(OWRegistry.blockKey("quartz_wall"))));

        QUARTZ_BRICK_WALL = OWRegistry.registerBlockWithItem("quartz_brick_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.QUARTZ_BRICKS).requiresTool().registryKey(OWRegistry.blockKey("quartz_brick_wall"))));

        SMOOTH_QUARTZ_WALL = OWRegistry.registerBlockWithItem("smooth_quartz_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.SMOOTH_QUARTZ).requiresTool().registryKey(OWRegistry.blockKey("smooth_quartz_wall"))));

        END_STONE_WALL = OWRegistry.registerBlockWithItem("end_stone_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.END_STONE).requiresTool().registryKey(OWRegistry.blockKey("end_stone_wall"))));

        PURPUR_WALL = OWRegistry.registerBlockWithItem("purpur_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.PURPUR_BLOCK).requiresTool().registryKey(OWRegistry.blockKey("purpur_wall"))));

        SMOOTH_BASALT_WALL = OWRegistry.registerBlockWithItem("smooth_basalt_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.SMOOTH_BASALT).requiresTool().registryKey(OWRegistry.blockKey("smooth_basalt_wall"))));

        PRISMARINE_BRICK_WALL = OWRegistry.registerBlockWithItem("prismarine_brick_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.PRISMARINE_BRICKS).requiresTool().registryKey(OWRegistry.blockKey("prismarine_brick_wall"))));

        DARK_PRISMARINE_WALL = OWRegistry.registerBlockWithItem("dark_prismarine_wall",
                new WallBlock(AbstractBlock.Settings.copy(Blocks.DARK_PRISMARINE).requiresTool().registryKey(OWRegistry.blockKey("dark_prismarine_wall"))));
    }
}