package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class CauldronCleansTrims {
    private CauldronCleansTrims() {}

    public static void init() {
        UseBlockCallback.EVENT.register(CauldronCleansTrims::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (player == null) return InteractionResult.PASS;

        final ItemStack stack = player.getItemInHand(hand);

        // Only proceed if the item actually has a trim applied to it
        if (!stack.has(DataComponents.TRIM)) return InteractionResult.PASS;

        final BlockPos pos = hit.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!state.is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;

        final int level = state.getValue(LayeredCauldronBlock.LEVEL);
        if (level <= 0) return InteractionResult.PASS;

        if (world.isClientSide()) return InteractionResult.SUCCESS;

        // Create a cleaned copy of the item
        ItemStack cleanedItem = stack.copy();
        cleanedItem.setCount(1);
        cleanedItem.remove(DataComponents.TRIM);
        cleanedItem.remove(ModDataComponents.EMISSIVE_TRIM);
        cleanedItem.remove(ModDataComponents.PULSING_TRIM);

        if (!player.getAbilities().instabuild) {
            // Decrease water level
            if (level > 1) {
                world.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, level - 1), Block.UPDATE_ALL);
            } else {
                world.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
            }

            // Replace or give the item
            if (stack.getCount() == 1) {
                player.setItemInHand(hand, cleanedItem);
            } else {
                stack.shrink(1);
                if (!player.getInventory().add(cleanedItem)) {
                    player.drop(cleanedItem, false);
                }
            }
        } else {
            // If in creative, we still want to clean the item, we just don't drain the water
            if (stack.getCount() == 1) {
                player.setItemInHand(hand, cleanedItem);
            } else {
                if (!player.getInventory().add(cleanedItem)) {
                    player.drop(cleanedItem, false);
                }
            }
        }

        // Play splashing sound and increment the vanilla cauldron cleaning stat
        world.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.awardStat(Stats.CLEAN_ARMOR);
        return InteractionResult.SUCCESS;
    }
}