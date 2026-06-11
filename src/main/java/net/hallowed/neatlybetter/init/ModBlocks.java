package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    private ModBlocks() {}

    public static Block SMOOTH_STONE_STAIRS, CALCITE_STAIRS, QUARTZ_BRICK_STAIRS, END_STONE_STAIRS, SMOOTH_BASALT_STAIRS,
                        TERRACOTTA_STAIRS,
                        WHITE_TERRACOTTA_STAIRS, LIGHT_GRAY_TERRACOTTA_STAIRS, GRAY_TERRACOTTA_STAIRS, BLACK_TERRACOTTA_STAIRS,
                        BROWN_TERRACOTTA_STAIRS, RED_TERRACOTTA_STAIRS, ORANGE_TERRACOTTA_STAIRS, YELLOW_TERRACOTTA_STAIRS,
                        LIME_TERRACOTTA_STAIRS, GREEN_TERRACOTTA_STAIRS, CYAN_TERRACOTTA_STAIRS, LIGHT_BLUE_TERRACOTTA_STAIRS,
                        BLUE_TERRACOTTA_STAIRS, PURPLE_TERRACOTTA_STAIRS, MAGENTA_TERRACOTTA_STAIRS, PINK_TERRACOTTA_STAIRS,
                        WHITE_CONCRETE_STAIRS, LIGHT_GRAY_CONCRETE_STAIRS, GRAY_CONCRETE_STAIRS, BLACK_CONCRETE_STAIRS,
                        BROWN_CONCRETE_STAIRS, RED_CONCRETE_STAIRS, ORANGE_CONCRETE_STAIRS, YELLOW_CONCRETE_STAIRS,
                        LIME_CONCRETE_STAIRS, GREEN_CONCRETE_STAIRS, CYAN_CONCRETE_STAIRS, LIGHT_BLUE_CONCRETE_STAIRS,
                        BLUE_CONCRETE_STAIRS, PURPLE_CONCRETE_STAIRS, MAGENTA_CONCRETE_STAIRS, PINK_CONCRETE_STAIRS;

    public static Block CALCITE_SLAB, QUARTZ_BRICK_SLAB, END_STONE_SLAB, SMOOTH_BASALT_SLAB, TERRACOTTA_SLAB,
                        WHITE_TERRACOTTA_SLAB, LIGHT_GRAY_TERRACOTTA_SLAB, GRAY_TERRACOTTA_SLAB, BLACK_TERRACOTTA_SLAB,
                        BROWN_TERRACOTTA_SLAB, RED_TERRACOTTA_SLAB, ORANGE_TERRACOTTA_SLAB, YELLOW_TERRACOTTA_SLAB,
                        LIME_TERRACOTTA_SLAB, GREEN_TERRACOTTA_SLAB, CYAN_TERRACOTTA_SLAB, LIGHT_BLUE_TERRACOTTA_SLAB,
                        BLUE_TERRACOTTA_SLAB, PURPLE_TERRACOTTA_SLAB, MAGENTA_TERRACOTTA_SLAB, PINK_TERRACOTTA_SLAB,
                        WHITE_CONCRETE_SLAB, LIGHT_GRAY_CONCRETE_SLAB, GRAY_CONCRETE_SLAB, BLACK_CONCRETE_SLAB,
                        BROWN_CONCRETE_SLAB, RED_CONCRETE_SLAB, ORANGE_CONCRETE_SLAB, YELLOW_CONCRETE_SLAB,
                        LIME_CONCRETE_SLAB, GREEN_CONCRETE_SLAB, CYAN_CONCRETE_SLAB, LIGHT_BLUE_CONCRETE_SLAB,
                        BLUE_CONCRETE_SLAB, PURPLE_CONCRETE_SLAB, MAGENTA_CONCRETE_SLAB, PINK_CONCRETE_SLAB;

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

        TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "terracotta_stairs",
                new StairBlock(Blocks.TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("terracotta_stairs")))
        );

        WHITE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "white_terracotta_stairs",
                new StairBlock(Blocks.WHITE_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_terracotta_stairs")))
        );

        LIGHT_GRAY_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "light_gray_terracotta_stairs",
                new StairBlock(Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_terracotta_stairs")))
        );

        GRAY_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "gray_terracotta_stairs",
                new StairBlock(Blocks.GRAY_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_terracotta_stairs")))
        );

        BLACK_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "black_terracotta_stairs",
                new StairBlock(Blocks.BLACK_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_terracotta_stairs")))
        );

        BROWN_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "brown_terracotta_stairs",
                new StairBlock(Blocks.BROWN_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_terracotta_stairs")))
        );

        RED_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "red_terracotta_stairs",
                new StairBlock(Blocks.RED_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.RED_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_terracotta_stairs")))
        );

        ORANGE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "orange_terracotta_stairs",
                new StairBlock(Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_terracotta_stairs")))
        );

        YELLOW_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "yellow_terracotta_stairs",
                new StairBlock(Blocks.YELLOW_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.YELLOW_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_terracotta_stairs")))
        );

        LIME_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "lime_terracotta_stairs",
                new StairBlock(Blocks.LIME_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIME_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_terracotta_stairs")))
        );

        GREEN_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "green_terracotta_stairs",
                new StairBlock(Blocks.GREEN_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.GREEN_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_terracotta_stairs")))
        );

        CYAN_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "cyan_terracotta_stairs",
                new StairBlock(Blocks.CYAN_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CYAN_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_terracotta_stairs")))
        );

        LIGHT_BLUE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "light_blue_terracotta_stairs",
                new StairBlock(Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_BLUE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_terracotta_stairs")))
        );

        BLUE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "blue_terracotta_stairs",
                new StairBlock(Blocks.BLUE_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_terracotta_stairs")))
        );

        PURPLE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "purple_terracotta_stairs",
                new StairBlock(Blocks.PURPLE_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.PURPLE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_terracotta_stairs")))
        );

        MAGENTA_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "magenta_terracotta_stairs",
                new StairBlock(Blocks.MAGENTA_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.MAGENTA_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_terracotta_stairs")))
        );

        PINK_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "pink_terracotta_stairs",
                new StairBlock(Blocks.PINK_TERRACOTTA.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_terracotta_stairs")))
        );

        WHITE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "white_concrete_stairs",
                new StairBlock(Blocks.WHITE_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_concrete_stairs")))
        );

        LIGHT_GRAY_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "light_gray_concrete_stairs",
                new StairBlock(Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_concrete_stairs")))
        );

        GRAY_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "gray_concrete_stairs",
                new StairBlock(Blocks.GRAY_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_concrete_stairs")))
        );

        BLACK_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "black_concrete_stairs",
                new StairBlock(Blocks.BLACK_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_concrete_stairs")))
        );

        BROWN_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "brown_concrete_stairs",
                new StairBlock(Blocks.BROWN_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_concrete_stairs")))
        );

        RED_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "red_concrete_stairs",
                new StairBlock(Blocks.RED_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.RED_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_concrete_stairs")))
        );

        ORANGE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "orange_concrete_stairs",
                new StairBlock(Blocks.ORANGE_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_concrete_stairs")))
        );

        YELLOW_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "yellow_concrete_stairs",
                new StairBlock(Blocks.YELLOW_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.YELLOW_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_concrete_stairs")))
        );

        LIME_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "lime_concrete_stairs",
                new StairBlock(Blocks.LIME_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIME_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_concrete_stairs")))
        );

        GREEN_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "green_concrete_stairs",
                new StairBlock(Blocks.GREEN_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.GREEN_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_concrete_stairs")))
        );

        CYAN_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "cyan_concrete_stairs",
                new StairBlock(Blocks.CYAN_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CYAN_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_concrete_stairs")))
        );

        LIGHT_BLUE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "light_blue_concrete_stairs",
                new StairBlock(Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_BLUE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_concrete_stairs")))
        );

        BLUE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "blue_concrete_stairs",
                new StairBlock(Blocks.BLUE_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_concrete_stairs")))
        );

        PURPLE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "purple_concrete_stairs",
                new StairBlock(Blocks.PURPLE_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.PURPLE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_concrete_stairs")))
        );

        MAGENTA_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "magenta_concrete_stairs",
                new StairBlock(Blocks.MAGENTA_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.MAGENTA_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_concrete_stairs")))
        );

        PINK_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "pink_concrete_stairs",
                new StairBlock(Blocks.PINK_CONCRETE.defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_concrete_stairs")))
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

        TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("terracotta_slab")))
        );

        WHITE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "white_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_terracotta_slab")))
        );

        LIGHT_GRAY_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "light_gray_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_terracotta_slab")))
        );

        GRAY_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "gray_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_terracotta_slab")))
        );

        BLACK_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "black_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_terracotta_slab")))
        );

        BROWN_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "brown_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_terracotta_slab")))
        );

        RED_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "red_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_terracotta_slab")))
        );

        ORANGE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "orange_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_terracotta_slab")))
        );

        YELLOW_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "yellow_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.YELLOW_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_terracotta_slab")))
        );

        LIME_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "lime_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIME_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_terracotta_slab")))
        );

        GREEN_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "green_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GREEN_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_terracotta_slab")))
        );

        CYAN_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "cyan_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CYAN_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_terracotta_slab")))
        );

        LIGHT_BLUE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "light_blue_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_BLUE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_terracotta_slab")))
        );

        BLUE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "blue_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_terracotta_slab")))
        );

        PURPLE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "purple_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PURPLE_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_terracotta_slab")))
        );

        MAGENTA_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "magenta_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MAGENTA_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_terracotta_slab")))
        );

        PINK_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "pink_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_TERRACOTTA).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_terracotta_slab")))
        );

        WHITE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "white_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_concrete_slab")))
        );

        LIGHT_GRAY_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "light_gray_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_concrete_slab")))
        );

        GRAY_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "gray_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_concrete_slab")))
        );

        BLACK_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "black_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_concrete_slab")))
        );

        BROWN_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "brown_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_concrete_slab")))
        );

        RED_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "red_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_concrete_slab")))
        );

        ORANGE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "orange_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_concrete_slab")))
        );

        YELLOW_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "yellow_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.YELLOW_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_concrete_slab")))
        );

        LIME_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "lime_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIME_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_concrete_slab")))
        );

        GREEN_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "green_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GREEN_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_concrete_slab")))
        );

        CYAN_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "cyan_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CYAN_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_concrete_slab")))
        );

        LIGHT_BLUE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "light_blue_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_BLUE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_concrete_slab")))
        );

        BLUE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "blue_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_concrete_slab")))
        );

        PURPLE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "purple_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PURPLE_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_concrete_slab")))
        );

        MAGENTA_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "magenta_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MAGENTA_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_concrete_slab")))
        );

        PINK_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "pink_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_CONCRETE).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_concrete_slab")))
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