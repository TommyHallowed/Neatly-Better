package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class AnvilRestoration {
    private AnvilRestoration() {}

    public static void register() {
        UseBlockCallback.EVENT.register(AnvilRestoration::onUseBlock);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!player.isInSneakingPose()) return ActionResult.PASS;
        if (!player.getStackInHand(hand).isOf(Items.IRON_BLOCK)) return ActionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        boolean canRepairDamaged = state.isOf(Blocks.DAMAGED_ANVIL);
        boolean canRepairChipped = state.isOf(Blocks.CHIPPED_ANVIL);

        if (!canRepairDamaged && !canRepairChipped) {
            return ActionResult.PASS;
        }

        // CLIENT: cancel placement prediction; don't modify world
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        // SERVER: perform the repair
        BlockState newState;
        if (canRepairDamaged) {
            newState = Blocks.CHIPPED_ANVIL.getDefaultState()
                    .with(AnvilBlock.FACING, state.get(AnvilBlock.FACING));
        } else {
            newState = Blocks.ANVIL.getDefaultState()
                    .with(AnvilBlock.FACING, state.get(AnvilBlock.FACING));
        }

        if (newState != state) {
            world.setBlockState(pos, newState);
            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                player.getStackInHand(hand).decrementUnlessCreative(1, player);
            return ActionResult.SUCCESS_SERVER;
        }

        return ActionResult.PASS;
    }
}
