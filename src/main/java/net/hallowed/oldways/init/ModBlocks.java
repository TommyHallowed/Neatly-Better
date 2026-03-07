package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block SMOOTH_STONE_STAIRS, CALCITE_STAIRS, QUARTZ_BRICK_STAIRS, END_STONE_STAIRS, SMOOTH_BASALT_STAIRS;

    public static Block CALCITE_SLAB, QUARTZ_BRICK_SLAB, END_STONE_SLAB, SMOOTH_BASALT_SLAB;

    public static Block POLISHED_GRANITE_WALL, POLISHED_ANDESITE_WALL, POLISHED_DIORITE_WALL, STONE_WALL,
            SMOOTH_STONE_WALL, CALCITE_WALL, QUARTZ_WALL, QUARTZ_BRICK_WALL, SMOOTH_QUARTZ_WALL,
            END_STONE_WALL, PURPUR_WALL, SMOOTH_BASALT_WALL, PRISMARINE_BRICK_WALL, DARK_PRISMARINE_WALL,
            SMOOTH_SANDSTONE_WALL, SMOOTH_RED_SANDSTONE_WALL;

    public static void register() {
        // Stairs
        SMOOTH_STONE_STAIRS = OWRegistry.registerBlockWithItem(
                "smooth_stone_stairs",
                new StairBlock(Blocks.SMOOTH_STONE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_stone_stairs")))
        );

        CALCITE_STAIRS = OWRegistry.registerBlockWithItem(
                "calcite_stairs",
                new StairBlock(Blocks.CALCITE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("calcite_stairs")))
        );

        QUARTZ_BRICK_STAIRS = OWRegistry.registerBlockWithItem(
                "quartz_brick_stairs",
                new StairBlock(Blocks.QUARTZ_BRICKS.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BRICKS).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("quartz_brick_stairs")))
        );

        END_STONE_STAIRS = OWRegistry.registerBlockWithItem(
                "end_stone_stairs",
                new StairBlock(Blocks.END_STONE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("end_stone_stairs")))
        );

        SMOOTH_BASALT_STAIRS = OWRegistry.registerBlockWithItem(
                "smooth_basalt_stairs",
                new StairBlock(Blocks.SMOOTH_BASALT.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_BASALT).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_basalt_stairs")))
        );

        // Slabs
        CALCITE_SLAB = OWRegistry.registerBlockWithItem(
                "calcite_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("calcite_slab")))
        );

        QUARTZ_BRICK_SLAB = OWRegistry.registerBlockWithItem(
                "quartz_brick_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BRICKS).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("quartz_brick_slab")))
        );

        END_STONE_SLAB = OWRegistry.registerBlockWithItem(
                "end_stone_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("end_stone_slab")))
        );

        SMOOTH_BASALT_SLAB = OWRegistry.registerBlockWithItem(
                "smooth_basalt_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_BASALT).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_basalt_slab")))
        );

        // Walls
        POLISHED_GRANITE_WALL = OWRegistry.registerBlockWithItem("polished_granite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_GRANITE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("polished_granite_wall"))));

        POLISHED_ANDESITE_WALL = OWRegistry.registerBlockWithItem("polished_andesite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("polished_andesite_wall"))));

        POLISHED_DIORITE_WALL = OWRegistry.registerBlockWithItem("polished_diorite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_DIORITE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("polished_diorite_wall"))));

        STONE_WALL = OWRegistry.registerBlockWithItem("stone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("stone_wall"))));

        SMOOTH_STONE_WALL = OWRegistry.registerBlockWithItem("smooth_stone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_stone_wall"))));

        CALCITE_WALL = OWRegistry.registerBlockWithItem("calcite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("calcite_wall"))));

        QUARTZ_WALL = OWRegistry.registerBlockWithItem("quartz_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("quartz_wall"))));

        QUARTZ_BRICK_WALL = OWRegistry.registerBlockWithItem("quartz_brick_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BRICKS).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("quartz_brick_wall"))));

        SMOOTH_QUARTZ_WALL = OWRegistry.registerBlockWithItem("smooth_quartz_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_quartz_wall"))));

        END_STONE_WALL = OWRegistry.registerBlockWithItem("end_stone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("end_stone_wall"))));

        PURPUR_WALL = OWRegistry.registerBlockWithItem("purpur_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PURPUR_BLOCK).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("purpur_wall"))));

        SMOOTH_BASALT_WALL = OWRegistry.registerBlockWithItem("smooth_basalt_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_BASALT).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_basalt_wall"))));

        PRISMARINE_BRICK_WALL = OWRegistry.registerBlockWithItem("prismarine_brick_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE_BRICKS).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("prismarine_brick_wall"))));

        DARK_PRISMARINE_WALL = OWRegistry.registerBlockWithItem("dark_prismarine_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("dark_prismarine_wall"))));

        SMOOTH_SANDSTONE_WALL = OWRegistry.registerBlockWithItem("smooth_sandstone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_SANDSTONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_sandstone_wall"))));

        SMOOTH_RED_SANDSTONE_WALL = OWRegistry.registerBlockWithItem("smooth_red_sandstone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE).requiresCorrectToolForDrops().setId(OWRegistry.blockKey("smooth_red_sandstone_wall"))));
    }
}