package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class CauldronCleaning {
    private CauldronCleaning() {}

    public static void init() {
        UseBlockCallback.EVENT.register(CauldronCleaning::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (player == null) return InteractionResult.PASS;

        final ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return InteractionResult.PASS;

        final ItemStack cleanedItem;
        final SoundEvent sound;
        final Identifier stat;

        if (stack.is(Items.FILLED_MAP)) {
            cleanedItem = new ItemStack(Items.MAP);
            sound = SoundEvents.BOOK_PAGE_TURN;
            stat = Stats.USE_CAULDRON;

        } else if (stack.has(DataComponents.TRIM)) {
            cleanedItem = stack.copy();
            cleanedItem.setCount(1);
            cleanedItem.remove(DataComponents.TRIM);
            cleanedItem.remove(ModData.EMISSIVE_TRIM);
            cleanedItem.remove(ModData.PULSING_TRIM);
            sound = SoundEvents.GENERIC_SPLASH;
            stat = Stats.CLEAN_ARMOR;

        } else {
            return InteractionResult.PASS;
        }

        final BlockPos pos = hit.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!state.is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;

        final int level = state.getValue(LayeredCauldronBlock.LEVEL);
        if (level <= 0) return InteractionResult.PASS;

        if (world.isClientSide()) return InteractionResult.SUCCESS;

        if (!player.getAbilities().instabuild) {
            if (level > 1) {
                world.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, level - 1), Block.UPDATE_ALL);
            } else {
                world.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        if (stack.getCount() == 1) {
            player.setItemInHand(hand, cleanedItem);
        } else {
            stack.shrink(1);
            if (!player.getInventory().add(cleanedItem)) {
                player.drop(cleanedItem, false);
            }
        }

        world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.awardStat(stat);
        return InteractionResult.SUCCESS;
    }
}
