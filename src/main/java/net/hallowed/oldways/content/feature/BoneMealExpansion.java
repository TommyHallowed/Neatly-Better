package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
            BlockPos below = pos.below();
            if (world.isEmptyBlock(below)) {
                grew = true;
                world.setBlock(below, copyVineFaces(state), Block.UPDATE_ALL);
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
            return getColumnHeight(world, pos, block) < 3 && world.isEmptyBlock(pos.above(getColumnHeight(world, pos, block)));
        }
        if (block instanceof VineBlock) {
            return world.isEmptyBlock(pos.below());
        }
        return false;
    }

    private static BlockPos growColumnOnce(Level world, BlockPos base, Block targetBlock) {
        int height = getColumnHeight(world, base, targetBlock);
        if (height >= 3) return null;
        BlockPos top = base.above(height);
        if (!world.isEmptyBlock(top)) return null;
        world.setBlock(top, targetBlock.defaultBlockState(), Block.UPDATE_ALL);
        return top;
    }

    private static int getColumnHeight(Level world, BlockPos pos, Block target) {
        int h = 0;
        if (world.getBlockState(pos).is(target)) {
            while (h < 16 && world.getBlockState(pos.above(h)).is(target)) h++;
        } else if (world.getBlockState(pos.above()).is(target)) {
            while (h < 16 && world.getBlockState(pos.above(1 + h)).is(target)) h++;
        }
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
