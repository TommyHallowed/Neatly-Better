package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.hallowed.neatlybetter.api.NTRegistry;

import net.hallowed.neatlybetter.content.block.DyeCauldronBlock;
import net.hallowed.neatlybetter.content.block.GlowTorchBlock;

import net.hallowed.neatlybetter.content.block.GlowWallTorchBlock;
import net.hallowed.neatlybetter.content.block.MilkCauldronBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

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

    public static Block RAINBOW_WOOL, RAINBOW_CARPET, RAINBOW_BED, CHARCOAL_BLOCK, GLOW_TORCH, GLOW_WALL_TORCH, MILK_CAULDRON, DYE_CAULDRON;

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
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_terracotta_stairs")))
        );

        LIGHT_GRAY_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "light_gray_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_terracotta_stairs")))
        );

        GRAY_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "gray_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_terracotta_stairs")))
        );

        BLACK_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "black_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLACK).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLACK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_terracotta_stairs")))
        );

        BROWN_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "brown_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_terracotta_stairs")))
        );

        RED_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "red_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.RED)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_terracotta_stairs")))
        );

        ORANGE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "orange_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_terracotta_stairs")))
        );

        YELLOW_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "yellow_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_terracotta_stairs")))
        );

        LIME_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "lime_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_terracotta_stairs")))
        );

        GREEN_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "green_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_terracotta_stairs")))
        );

        CYAN_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "cyan_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.CYAN).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.CYAN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_terracotta_stairs")))
        );

        LIGHT_BLUE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "light_blue_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_terracotta_stairs")))
        );

        BLUE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "blue_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_terracotta_stairs")))
        );

        PURPLE_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "purple_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.PURPLE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.PURPLE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_terracotta_stairs")))
        );

        MAGENTA_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "magenta_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_terracotta_stairs")))
        );

        PINK_TERRACOTTA_STAIRS = NTRegistry.registerBlockWithItem(
                "pink_terracotta_stairs",
                new StairBlock(Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_terracotta_stairs")))
        );

        WHITE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "white_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.WHITE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_concrete_stairs")))
        );

        LIGHT_GRAY_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "light_gray_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.LIGHT_GRAY).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.LIGHT_GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_concrete_stairs")))
        );

        GRAY_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "gray_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.GRAY).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_concrete_stairs")))
        );

        BLACK_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "black_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.BLACK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_concrete_stairs")))
        );

        BROWN_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "brown_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.BROWN).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.BROWN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_concrete_stairs")))
        );

        RED_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "red_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.RED).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.RED)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_concrete_stairs")))
        );

        ORANGE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "orange_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.ORANGE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.ORANGE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_concrete_stairs")))
        );

        YELLOW_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "yellow_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.YELLOW).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.YELLOW)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_concrete_stairs")))
        );

        LIME_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "lime_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.LIME).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.LIME)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_concrete_stairs")))
        );

        GREEN_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "green_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.GREEN).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.GREEN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_concrete_stairs")))
        );

        CYAN_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "cyan_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.CYAN).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.CYAN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_concrete_stairs")))
        );

        LIGHT_BLUE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "light_blue_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_concrete_stairs")))
        );

        BLUE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "blue_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.BLUE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_concrete_stairs")))
        );

        PURPLE_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "purple_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.PURPLE).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.PURPLE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_concrete_stairs")))
        );

        MAGENTA_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "magenta_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.MAGENTA).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.MAGENTA)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_concrete_stairs")))
        );

        PINK_CONCRETE_STAIRS = NTRegistry.registerBlockWithItem(
                "pink_concrete_stairs",
                new StairBlock(Blocks.CONCRETE.pick(DyeColor.PINK).defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.PINK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_concrete_stairs")))
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
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_terracotta_slab")))
        );

        LIGHT_GRAY_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "light_gray_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_terracotta_slab")))
        );

        GRAY_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "gray_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_terracotta_slab")))
        );

        BLACK_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "black_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLACK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_terracotta_slab")))
        );

        BROWN_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "brown_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_terracotta_slab")))
        );

        RED_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "red_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.RED)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_terracotta_slab")))
        );

        ORANGE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "orange_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_terracotta_slab")))
        );

        YELLOW_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "yellow_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_terracotta_slab")))
        );

        LIME_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "lime_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_terracotta_slab")))
        );

        GREEN_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "green_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_terracotta_slab")))
        );

        CYAN_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "cyan_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.CYAN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_terracotta_slab")))
        );

        LIGHT_BLUE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "light_blue_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_terracotta_slab")))
        );

        BLUE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "blue_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_terracotta_slab")))
        );

        PURPLE_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "purple_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.PURPLE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_terracotta_slab")))
        );

        MAGENTA_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "magenta_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_terracotta_slab")))
        );

        PINK_TERRACOTTA_SLAB = NTRegistry.registerBlockWithItem(
                "pink_terracotta_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_terracotta_slab")))
        );

        WHITE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "white_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.WHITE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("white_concrete_slab")))
        );

        LIGHT_GRAY_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "light_gray_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.LIGHT_GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_gray_concrete_slab")))
        );

        GRAY_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "gray_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.GRAY)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("gray_concrete_slab")))
        );

        BLACK_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "black_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.BLACK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("black_concrete_slab")))
        );

        BROWN_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "brown_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.BROWN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("brown_concrete_slab")))
        );

        RED_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "red_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.RED)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("red_concrete_slab")))
        );

        ORANGE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "orange_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.ORANGE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("orange_concrete_slab")))
        );

        YELLOW_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "yellow_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.YELLOW)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("yellow_concrete_slab")))
        );

        LIME_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "lime_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.LIME)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("lime_concrete_slab")))
        );

        GREEN_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "green_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.GREEN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("green_concrete_slab")))
        );

        CYAN_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "cyan_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.CYAN)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("cyan_concrete_slab")))
        );

        LIGHT_BLUE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "light_blue_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("light_blue_concrete_slab")))
        );

        BLUE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "blue_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.BLUE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("blue_concrete_slab")))
        );

        PURPLE_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "purple_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.PURPLE)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("purple_concrete_slab")))
        );

        MAGENTA_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "magenta_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.MAGENTA)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("magenta_concrete_slab")))
        );

        PINK_CONCRETE_SLAB = NTRegistry.registerBlockWithItem(
                "pink_concrete_slab",
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.pick(DyeColor.PINK)).requiresCorrectToolForDrops().setId(NTRegistry.blockKey("pink_concrete_slab")))
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
                new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(NTRegistry.blockKey("rainbow_wool"))));

        RAINBOW_CARPET = NTRegistry.registerBlockWithItem("rainbow_carpet",
                new WoolCarpetBlock(DyeColor.WHITE, BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.WHITE)).setId(NTRegistry.blockKey("rainbow_carpet"))));

        RAINBOW_BED = NTRegistry.registerBlock("rainbow_bed",
                new BedBlock(DyeColor.WHITE,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.BED.pick(DyeColor.WHITE))
                                .setId(NTRegistry.blockKey("rainbow_bed"))));

        CHARCOAL_BLOCK = NTRegistry.registerBlockWithItem("charcoal_block",
                new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK).setId(NTRegistry.blockKey("charcoal_block"))));

        FuelValueEvents.BUILD.register((builder, _) -> builder.add(ModBlocks.CHARCOAL_BLOCK.asItem(), 16000));

        GLOW_TORCH = NTRegistry.registerBlock("glow_torch",
                new GlowTorchBlock(ParticleTypes.GLOW, BlockBehaviour.Properties.of()
                        .noCollision()
                        .instabreak()
                        .sound(SoundType.WOOD)
                        .lightLevel(state -> 15)
                        .pushReaction(PushReaction.DESTROY)
                        .setId(NTRegistry.blockKey("glow_torch"))));

        GLOW_WALL_TORCH = NTRegistry.registerBlock("glow_wall_torch",
                new GlowWallTorchBlock(ParticleTypes.GLOW, BlockBehaviour.Properties.of()
                        .noCollision()
                        .instabreak()
                        .sound(SoundType.WOOD)
                        .lightLevel(state -> 15)
                        .pushReaction(PushReaction.DESTROY)
                        .setId(NTRegistry.blockKey("glow_wall_torch"))));

        MILK_CAULDRON = NTRegistry.registerBlock("milk_cauldron",
                new MilkCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WATER_CAULDRON).setId(NTRegistry.blockKey("milk_cauldron"))));

        DYE_CAULDRON = NTRegistry.registerBlock("dye_cauldron",
                new DyeCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WATER_CAULDRON).setId(NTRegistry.blockKey("dye_cauldron"))));
    }
}