package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block SMOOTH_STONE_STAIRS, CALCITE_STAIRS, QUARTZ_BRICK_STAIRS, END_STONE_STAIRS, SMOOTH_BASALT_STAIRS;

    public static Block CALCITE_SLAB, QUARTZ_BRICK_SLAB, END_STONE_SLAB, SMOOTH_BASALT_SLAB;

    public static Block POLISHED_GRANITE_WALL, POLISHED_ANDESITE_WALL, POLISHED_DIORITE_WALL, STONE_WALL,
            SMOOTH_STONE_WALL, CALCITE_WALL, QUARTZ_WALL, QUARTZ_BRICK_WALL, SMOOTH_QUARTZ_WALL,
            END_STONE_WALL, PURPUR_WALL, SMOOTH_BASALT_WALL, PRISMARINE_BRICK_WALL, DARK_PRISMARINE_WALL,
            SMOOTH_SANDSTONE_WALL, SMOOTH_RED_SANDSTONE_WALL;

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED;

    public static void register() {
        // Stairs
        SMOOTH_STONE_STAIRS = NTRegistry.registerBlockWithItem(
                "smooth_stone_stairs",
                new StairBlock(Blocks.SMOOTH_STONE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_stone_stairs")))
        );

        CALCITE_STAIRS = NTRegistry.registerBlockWithItem(
                "calcite_stairs",
                new StairBlock(Blocks.CALCITE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("calcite_stairs")))
        );

        QUARTZ_BRICK_STAIRS = NTRegistry.registerBlockWithItem(
                "quartz_brick_stairs",
                new StairBlock(Blocks.QUARTZ_BRICKS.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BRICKS).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("quartz_brick_stairs")))
        );

        END_STONE_STAIRS = NTRegistry.registerBlockWithItem(
                "end_stone_stairs",
                new StairBlock(Blocks.END_STONE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("end_stone_stairs")))
        );

        SMOOTH_BASALT_STAIRS = NTRegistry.registerBlockWithItem(
                "smooth_basalt_stairs",
                new StairBlock(Blocks.SMOOTH_BASALT.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_BASALT).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_basalt_stairs")))
        );

        // Slabs
        CALCITE_SLAB = NTRegistry.registerBlockWithItem(
                "calcite_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("calcite_slab")))
        );

        QUARTZ_BRICK_SLAB = NTRegistry.registerBlockWithItem(
                "quartz_brick_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BRICKS).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("quartz_brick_slab")))
        );

        END_STONE_SLAB = NTRegistry.registerBlockWithItem(
                "end_stone_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("end_stone_slab")))
        );

        SMOOTH_BASALT_SLAB = NTRegistry.registerBlockWithItem(
                "smooth_basalt_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_BASALT).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_basalt_slab")))
        );

        // Walls
        POLISHED_GRANITE_WALL = NTRegistry.registerBlockWithItem("polished_granite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_GRANITE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("polished_granite_wall"))));

        POLISHED_ANDESITE_WALL = NTRegistry.registerBlockWithItem("polished_andesite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("polished_andesite_wall"))));

        POLISHED_DIORITE_WALL = NTRegistry.registerBlockWithItem("polished_diorite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_DIORITE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("polished_diorite_wall"))));

        STONE_WALL = NTRegistry.registerBlockWithItem("stone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("stone_wall"))));

        SMOOTH_STONE_WALL = NTRegistry.registerBlockWithItem("smooth_stone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_stone_wall"))));

        CALCITE_WALL = NTRegistry.registerBlockWithItem("calcite_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("calcite_wall"))));

        QUARTZ_WALL = NTRegistry.registerBlockWithItem("quartz_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("quartz_wall"))));

        QUARTZ_BRICK_WALL = NTRegistry.registerBlockWithItem("quartz_brick_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BRICKS).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("quartz_brick_wall"))));

        SMOOTH_QUARTZ_WALL = NTRegistry.registerBlockWithItem("smooth_quartz_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_quartz_wall"))));

        END_STONE_WALL = NTRegistry.registerBlockWithItem("end_stone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("end_stone_wall"))));

        PURPUR_WALL = NTRegistry.registerBlockWithItem("purpur_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PURPUR_BLOCK).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purpur_wall"))));

        SMOOTH_BASALT_WALL = NTRegistry.registerBlockWithItem("smooth_basalt_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_BASALT).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_basalt_wall"))));

        PRISMARINE_BRICK_WALL = NTRegistry.registerBlockWithItem("prismarine_brick_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE_BRICKS).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("prismarine_brick_wall"))));

        DARK_PRISMARINE_WALL = NTRegistry.registerBlockWithItem("dark_prismarine_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("dark_prismarine_wall"))));

        SMOOTH_SANDSTONE_WALL = NTRegistry.registerBlockWithItem("smooth_sandstone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_SANDSTONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_sandstone_wall"))));

        SMOOTH_RED_SANDSTONE_WALL = NTRegistry.registerBlockWithItem("smooth_red_sandstone_wall",
                new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("smooth_red_sandstone_wall"))));

        RAINBOW_WOOL = NTRegistry.registerBlockWithItem("rainbow_wool",
                new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).setId(NTRegistry.blockKey("rainbow_wool"))));

        RAINBOW_CARPET = NTRegistry.registerBlockWithItem("rainbow_carpet",
                new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CARPET).setId(NTRegistry.blockKey("rainbow_carpet"))));

        RAINBOW_BED = NTRegistry.registerBlock("rainbow_bed",
                new BedBlock(DyeColor.WHITE,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_BED)
                                .setId(NTRegistry.blockKey("rainbow_bed"))));
        BlockEntityType.BED.addValidBlock(RAINBOW_BED);
    }
}