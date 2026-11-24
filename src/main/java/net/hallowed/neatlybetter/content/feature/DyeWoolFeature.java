package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.EnumMap;
import java.util.Map;

public final class DyeWoolFeature {
    private DyeWoolFeature() {}

    /** Vanilla wool / carpet / bed per color, looked up once. */
    private static final Map<DyeColor, Block> WOOL   = byColor("_wool");
    private static final Map<DyeColor, Block> CARPET = byColor("_carpet");
    private static final Map<DyeColor, Block> BED    = byColor("_bed");

    private static Map<DyeColor, Block> byColor(String suffix) {
        Map<DyeColor, Block> map = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace(color.getName() + suffix))
                    .ifPresent(block -> map.put(color, block));
        }
        return map;
    }

    public static void register() {

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (NTServerConfig.CONFIG.inWorldWoolDyeing.isFalse()) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);

            if (!(stack.getItem() instanceof DyeItem)) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);

            boolean isWool   = state.is(BlockTags.WOOL);
            boolean isCarpet = state.is(BlockTags.WOOL_CARPETS);
            boolean isBed    = state.is(BlockTags.BEDS);

            if (!isWool && !isCarpet && !isBed) return InteractionResult.PASS;

            DyeColor color = stack.get(DataComponents.DYE);
            if (color == null) return InteractionResult.PASS;

            Block newBlock = (isWool ? WOOL : isCarpet ? CARPET : BED).get(color);
            if (newBlock == null) return InteractionResult.PASS;

            if (state.getBlock() == newBlock) return InteractionResult.PASS;

            if (!level.isClientSide()) {
                if (isBed) {
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    BedPart part     = state.getValue(BlockStateProperties.BED_PART);
                    boolean occupied = state.getValue(BlockStateProperties.OCCUPIED);

                    BlockPos footPos, headPos;
                    if (part == BedPart.FOOT) {
                        footPos = pos;
                        headPos = pos.relative(facing);
                    } else {
                        headPos = pos;
                        footPos = pos.relative(facing.getOpposite());
                    }

                    BlockState newFoot = newBlock.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                            .setValue(BlockStateProperties.BED_PART, BedPart.FOOT)
                            .setValue(BlockStateProperties.OCCUPIED, occupied);
                    BlockState newHead = newBlock.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                            .setValue(BlockStateProperties.BED_PART, BedPart.HEAD)
                            .setValue(BlockStateProperties.OCCUPIED, occupied);

                    level.setBlock(footPos, Blocks.AIR.defaultBlockState(), 22);
                    level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 22);

                    level.setBlock(headPos, newHead, 22);

                    level.setBlock(footPos, newFoot, 19);

                } else {
                    level.setBlock(pos, newBlock.defaultBlockState(), 3);
                }

                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }

            return InteractionResult.SUCCESS_SERVER;
        });

    }
}