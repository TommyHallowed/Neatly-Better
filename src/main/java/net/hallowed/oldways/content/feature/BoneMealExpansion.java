package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Bone-meal support for blocks that normally don't accept it:
 * - Nether Wart: +1 age (up to max)
 * - Cactus: grow column by +1 up to vanilla max height 3
 * - Sugar Cane: grow column by +1 up to vanilla max height 3
 * - Vines: extend downward by 1 if air below, copying facing properties
 */
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

        // If you clicked air above a plant (e.g., on upper hitbox), check block below
        if (state.isAir()) {
            pos = pos.down();
            state = world.getBlockState(pos);
            block = state.getBlock();
        }

        // Only run on server when we’re actually going to do something
        if (world.isClient) {
            if (isTarget(block, state, world, pos)) return ActionResult.SUCCESS;
            return ActionResult.PASS;
        }

        boolean grew = false;

        if (block == Blocks.NETHER_WART) {
            // Nether Wart: age 0..3
            IntProperty AGE = NetherWartBlock.AGE;
            int age = state.get(AGE);
            if (age < 3) {
                grew = true;
                world.setBlockState(pos, state.with(AGE, Math.min(3, age + 1)), Block.NOTIFY_ALL);
            }
        } else if (block == Blocks.CACTUS) {
            grew = growColumnOnce(world, pos, Blocks.CACTUS);
        } else if (block == Blocks.SUGAR_CANE) {
            grew = growColumnOnce(world, pos, Blocks.SUGAR_CANE);
        } else if (block instanceof VineBlock) {
            // Grow vines down by 1 if air directly below
            BlockPos below = pos.down();
            if (world.isAir(below)) {
                grew = true;
                world.setBlockState(below, copyVineFaces(state), Block.NOTIFY_ALL);
            }
        } else {
            return ActionResult.PASS; // Not our target
        }

        if (grew) {
            if (!player.isCreative()) stack.decrement(1);
            spawnBonemealParticles(world, pos);
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

    private static boolean growColumnOnce(World world, BlockPos base, Block targetBlock) {
        int height = getColumnHeight(world, base, targetBlock);
        if (height >= 3) return false; // vanilla natural max
        BlockPos top = base.up(height);
        if (!world.isAir(top)) return false;
        world.setBlockState(top, targetBlock.getDefaultState(), Block.NOTIFY_ALL);
        return true;
    }

    private static int getColumnHeight(World world, BlockPos pos, Block target) {
        // Find how many contiguous target blocks starting at pos and going upwards (including pos if same type)
        int h = 0;
        BlockPos cur = pos;
        BlockState st = world.getBlockState(cur);
        if (st.isOf(target)) {
            while (h < 16 && world.getBlockState(pos.up(h)).isOf(target)) { // 16 cap avoids weird towers
                h++;
            }
        } else if (world.getBlockState(cur.up()).isOf(target)) {
            // if clicked the ground next to a column, climb up one
            while (h < 16 && world.getBlockState(pos.up(1 + h)).isOf(target)) {
                h++;
            }
        }
        return h;
    }

    private static BlockState copyVineFaces(BlockState from) {
        BlockState st = Blocks.VINE.getDefaultState();
        // Copy which faces the current vine is attached to (N/E/S/W, and UP if present)
        for (Direction d : Direction.values()) {
            if (VineBlock.FACING_PROPERTIES.containsKey(d)) {
                st = st.with(VineBlock.FACING_PROPERTIES.get(d), from.get(VineBlock.FACING_PROPERTIES.get(d)));
            }
        }
        return st;
    }

    private static void spawnBonemealParticles(World world, BlockPos pos) {
        if (world instanceof ServerWorld sw) {
            // 1505 = bone meal particles event
            sw.syncWorldEvent(1505, pos, 0);
        }
    }
}
