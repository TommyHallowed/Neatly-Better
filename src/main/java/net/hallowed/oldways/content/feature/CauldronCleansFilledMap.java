package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
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

public final class CauldronCleansFilledMap {
    private CauldronCleansFilledMap() {}

    public static void init() {
        UseBlockCallback.EVENT.register(CauldronCleansFilledMap::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (player == null) return InteractionResult.PASS;

        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.FILLED_MAP)) return InteractionResult.PASS;

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

            ItemStack emptyMap = new ItemStack(Items.MAP);
            if (stack.getCount() == 1) {
                player.setItemInHand(hand, emptyMap);
            } else {
                stack.shrink(1);
                if (!player.getInventory().add(emptyMap)) {
                    player.drop(emptyMap, false);
                }
            }
        }

        world.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.awardStat(Stats.USE_CAULDRON);
        return InteractionResult.SUCCESS;
    }
}
