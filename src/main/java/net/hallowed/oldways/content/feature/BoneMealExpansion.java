package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public final class BoneMealExpansion {
    private BoneMealExpansion() {}

    public static void init() {
        UseBlockCallback.EVENT.register(BoneMealExpansion::onUseBlockWithBoneMeal);
    }

    private static ActionResult onUseBlockWithBoneMeal(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isEmpty() || stack.getItem() != Items.BONE_MEAL) return ActionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (state.isAir()) {
            pos = pos.down();
            state = world.getBlockState(pos);
            block = state.getBlock();
        }

        if (world.isClient()) {
            if (isTarget(block, state, world, pos)) return ActionResult.SUCCESS;
            return ActionResult.PASS;
        }

        boolean grew = false;
        BlockPos particlePos = null;

        if (block == Blocks.NETHER_WART) {
            IntProperty AGE = NetherWartBlock.AGE;
            int age = state.get(AGE);
            if (age < 3) {
                grew = true;
                world.setBlockState(pos, state.with(AGE, Math.min(3, age + 1)), Block.NOTIFY_ALL);
                particlePos = pos;
            }
        } else if (block == Blocks.CACTUS) {
            BlockPos top = growColumnOnce(world, pos, Blocks.CACTUS);
            if (top != null) { grew = true; particlePos = top; }
        } else if (block == Blocks.SUGAR_CANE) {
            BlockPos top = growColumnOnce(world, pos, Blocks.SUGAR_CANE);
            if (top != null) { grew = true; particlePos = top; }
        } else if (block instanceof VineBlock) {
            BlockPos below = pos.down();
            if (world.isAir(below)) {
                grew = true;
                world.setBlockState(below, copyVineFaces(state), Block.NOTIFY_ALL);
                particlePos = below;
            }
        } else {
            return ActionResult.PASS;
        }

        if (grew) {
            player.getStackInHand(hand).decrementUnlessCreative(1, player);
            BlockPos fx = particlePos != null ? particlePos : pos;
            spawnBonemealParticles(world, fx);
            playBonemealSound(world, fx);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private static boolean isTarget(Block block, BlockState state, World world, BlockPos pos) {
        if (block == Blocks.NETHER_WART) {
            return state.get(NetherWartBlock.AGE) < 3;
        }
        if (block == Blocks.CACTUS || block == Blocks.SUGAR_CANE) {
            return getColumnHeight(world, pos, block) < 3 && world.isAir(pos.up(getColumnHeight(world, pos, block)));
        }
        if (block instanceof VineBlock) {
            return world.isAir(pos.down());
        }
        return false;
    }

    private static BlockPos growColumnOnce(World world, BlockPos base, Block targetBlock) {
        int height = getColumnHeight(world, base, targetBlock);
        if (height >= 3) return null;
        BlockPos top = base.up(height);
        if (!world.isAir(top)) return null;
        world.setBlockState(top, targetBlock.getDefaultState(), Block.NOTIFY_ALL);
        return top;
    }

    private static int getColumnHeight(World world, BlockPos pos, Block target) {
        int h = 0;
        if (world.getBlockState(pos).isOf(target)) {
            while (h < 16 && world.getBlockState(pos.up(h)).isOf(target)) h++;
        } else if (world.getBlockState(pos.up()).isOf(target)) {
            while (h < 16 && world.getBlockState(pos.up(1 + h)).isOf(target)) h++;
        }
        return h;
    }

    private static BlockState copyVineFaces(BlockState from) {
        BlockState st = Blocks.VINE.getDefaultState();
        for (Direction d : Direction.values()) {
            if (VineBlock.FACING_PROPERTIES.containsKey(d)) {
                st = st.with(VineBlock.FACING_PROPERTIES.get(d), from.get(VineBlock.FACING_PROPERTIES.get(d)));
            }
        }
        return st;
    }

    private static void spawnBonemealParticles(World world, BlockPos pos) {
        if (world instanceof ServerWorld sw) {
            sw.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
            sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.0);
        }
    }

    private static void playBonemealSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }
}
