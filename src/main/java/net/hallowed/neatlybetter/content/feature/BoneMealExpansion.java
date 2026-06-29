package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.api.NTCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class BoneMealExpansion {
    private BoneMealExpansion() {}

    public static void init() {
        UseBlockCallback.EVENT.register(BoneMealExpansion::onUseBlockWithBoneMeal);
    }

    private static InteractionResult onUseBlockWithBoneMeal(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (NTCompat.EXTENDEDBONEMEAL || NTCompat.UNIVERSALBONEMEAL) return InteractionResult.PASS;
        if (stack.isEmpty() || stack.getItem() != Items.BONE_MEAL) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (state.isAir()) {
            pos = pos.below();
            state = world.getBlockState(pos);
            block = state.getBlock();
        }

        if (world.isClientSide()) {
            if (isTarget(block, state, world, pos)) return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        }

        boolean grew = false;
        BlockPos particlePos = null;

        if (block == Blocks.NETHER_WART) {
            IntegerProperty AGE = NetherWartBlock.AGE;
            int age = state.getValue(AGE);
            if (age < 3) {
                grew = true;
                world.setBlock(pos, state.setValue(AGE, Math.min(3, age + 1)), Block.UPDATE_ALL);
                particlePos = pos;
            }
        } else if (block == Blocks.CACTUS) {
            BlockPos top = growColumnOnce(world, pos, Blocks.CACTUS);
            if (top != null) { grew = true; particlePos = top; }
        } else if (block == Blocks.SUGAR_CANE) {
            BlockPos top = growColumnOnce(world, pos, Blocks.SUGAR_CANE);
            if (top != null) { grew = true; particlePos = top; }
        } else if (block instanceof VineBlock) {
            BlockPos bottom = getColumnBottom(world, pos, Blocks.VINE);
            BlockPos below = bottom.below();
            if (world.isEmptyBlock(below)) {
                grew = true;
                world.setBlock(below, copyVineFaces(world.getBlockState(bottom)), Block.UPDATE_ALL);
                particlePos = below;
            }
        } else {
            return InteractionResult.PASS;
        }

        if (grew) {
            player.getItemInHand(hand).consume(1, player);
            spawnBonemealParticles(world, particlePos);
            playBonemealSound(world, particlePos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static boolean isTarget(Block block, BlockState state, Level world, BlockPos pos) {
        if (block == Blocks.NETHER_WART) {
            return state.getValue(NetherWartBlock.AGE) < 3;
        }
        if (block == Blocks.CACTUS || block == Blocks.SUGAR_CANE) {
            BlockPos bottom = getColumnBottom(world, pos, block);
            int height = getColumnHeight(world, bottom, block);
            return height < 16 && world.isEmptyBlock(bottom.above(height));
        }
        if (block instanceof VineBlock) {
            return world.isEmptyBlock(getColumnBottom(world, pos, Blocks.VINE).below());
        }
        return false;
    }

    private static BlockPos growColumnOnce(Level world, BlockPos pos, Block targetBlock) {
        BlockPos bottom = getColumnBottom(world, pos, targetBlock);
        int height = getColumnHeight(world, bottom, targetBlock);
        if (height >= 16) return null;
        BlockPos top = bottom.above(height);
        if (!world.isEmptyBlock(top)) return null;
        world.setBlock(top, targetBlock.defaultBlockState(), Block.UPDATE_ALL);
        return top;
    }

    private static BlockPos getColumnBottom(Level world, BlockPos pos, Block target) {
        while (world.getBlockState(pos.below()).is(target)) pos = pos.below();
        return pos;
    }

    private static int getColumnHeight(Level world, BlockPos bottom, Block target) {
        int h = 0;
        while (h < 16 && world.getBlockState(bottom.above(h)).is(target)) h++;
        return h;
    }

    private static BlockState copyVineFaces(BlockState from) {
        BlockState st = Blocks.VINE.defaultBlockState();
        for (Direction d : Direction.values()) {
            if (VineBlock.PROPERTY_BY_DIRECTION.containsKey(d)) {
                st = st.setValue(VineBlock.PROPERTY_BY_DIRECTION.get(d), from.getValue(VineBlock.PROPERTY_BY_DIRECTION.get(d)));
            }
        }
        return st;
    }

    private static void spawnBonemealParticles(Level world, BlockPos pos) {
        if (world instanceof ServerLevel sw) {
            sw.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
            sw.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.0);
        }
    }

    private static void playBonemealSound(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}