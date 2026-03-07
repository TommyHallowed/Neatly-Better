package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class AnvilRestoration {
    private AnvilRestoration() {}

    public static void register() {
        UseBlockCallback.EVENT.register(AnvilRestoration::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!player.isCrouching()) return InteractionResult.PASS;
        if (!player.getItemInHand(hand).is(Items.IRON_BLOCK)) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        boolean canRepairDamaged = state.is(Blocks.DAMAGED_ANVIL);
        boolean canRepairChipped = state.is(Blocks.CHIPPED_ANVIL);

        if (!canRepairDamaged && !canRepairChipped) {
            return InteractionResult.PASS;
        }

        // CLIENT: cancel placement prediction; don't modify world
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // SERVER: perform the repair
        BlockState newState;
        if (canRepairDamaged) {
            newState = Blocks.CHIPPED_ANVIL.defaultBlockState()
                    .setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
        } else {
            newState = Blocks.ANVIL.defaultBlockState()
                    .setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
        }

        if (newState != state) {
            world.setBlockAndUpdate(pos, newState);
            world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.getItemInHand(hand).consume(1, player);
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }
}
