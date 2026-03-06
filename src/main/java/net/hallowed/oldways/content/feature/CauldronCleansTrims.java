package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CauldronCleansTrims {
    private CauldronCleansTrims() {}

    public static void init() {
        UseBlockCallback.EVENT.register(CauldronCleansTrims::onUseBlock);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (player == null) return ActionResult.PASS;

        final ItemStack stack = player.getStackInHand(hand);

        // Only proceed if the item actually has a trim applied to it
        if (!stack.contains(DataComponentTypes.TRIM)) return ActionResult.PASS;

        final BlockPos pos = hit.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.WATER_CAULDRON)) return ActionResult.PASS;

        final int level = state.get(LeveledCauldronBlock.LEVEL);
        if (level <= 0) return ActionResult.PASS;

        if (world.isClient()) return ActionResult.SUCCESS;

        // Create a cleaned copy of the item
        ItemStack cleanedItem = stack.copy();
        cleanedItem.setCount(1);
        cleanedItem.remove(DataComponentTypes.TRIM);
        cleanedItem.remove(ModDataComponents.EMISSIVE_TRIM);
        cleanedItem.remove(ModDataComponents.PULSING_TRIM);

        if (!player.getAbilities().creativeMode) {
            // Decrease water level
            if (level > 1) {
                world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, level - 1), Block.NOTIFY_ALL);
            } else {
                world.setBlockState(pos, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
            }

            // Replace or give the item
            if (stack.getCount() == 1) {
                player.setStackInHand(hand, cleanedItem);
            } else {
                stack.decrement(1);
                if (!player.getInventory().insertStack(cleanedItem)) {
                    player.dropItem(cleanedItem, false);
                }
            }
        } else {
            // If in creative, we still want to clean the item, we just don't drain the water
            if (stack.getCount() == 1) {
                player.setStackInHand(hand, cleanedItem);
            } else {
                if (!player.getInventory().insertStack(cleanedItem)) {
                    player.dropItem(cleanedItem, false);
                }
            }
        }

        // Play splashing sound and increment the vanilla cauldron cleaning stat
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        player.incrementStat(Stats.CLEAN_ARMOR);
        return ActionResult.SUCCESS;
    }
}