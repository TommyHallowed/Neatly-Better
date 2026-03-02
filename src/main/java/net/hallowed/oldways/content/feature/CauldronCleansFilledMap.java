package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CauldronCleansFilledMap {
    private CauldronCleansFilledMap() {}

    public static void init() {
        UseBlockCallback.EVENT.register(CauldronCleansFilledMap::onUseBlock);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (player == null) return ActionResult.PASS;

        final ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(Items.FILLED_MAP)) return ActionResult.PASS;

        final BlockPos pos = hit.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.WATER_CAULDRON)) return ActionResult.PASS;

        final int level = state.get(LeveledCauldronBlock.LEVEL);
        if (level <= 0) return ActionResult.PASS;

        if (world.isClient()) return ActionResult.SUCCESS;

        if (!player.getAbilities().creativeMode) {
            if (level > 1) {
                world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, level - 1), Block.NOTIFY_ALL);
            } else {
                world.setBlockState(pos, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
            }

            ItemStack emptyMap = new ItemStack(Items.MAP);
            if (stack.getCount() == 1) {
                player.setStackInHand(hand, emptyMap);
            } else {
                stack.decrement(1);
                if (!player.getInventory().insertStack(emptyMap)) {
                    player.dropItem(emptyMap, false);
                }
            }
        }

        world.playSound(null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0F, 1.0F);
        player.incrementStat(Stats.USE_CAULDRON);
        return ActionResult.SUCCESS;
    }
}
