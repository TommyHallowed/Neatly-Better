package net.hallowed.neatlybetter.feature.building;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.compat.NTCompat;
import net.hallowed.neatlybetter.config.NTServerConfig;

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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public final class AnvilRepair {
    private AnvilRepair() {}

    public static void register() {
        UseBlockCallback.EVENT.register(AnvilRepair::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (NTServerConfig.CONFIG.anvilRepair.isFalse()) return InteractionResult.PASS;
        if (NTCompat.ANVILRESTORATION || NTCompat.EASYANVILS) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        // Same check vanilla uses to decide between using the block and placing the item,
        // so client and server always agree (pose-based isCrouching() can lag a tick on the server).
        if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!player.getItemInHand(hand).is(Items.IRON_BLOCK)) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        boolean canRepairDamaged = state.is(Blocks.DAMAGED_ANVIL);
        boolean canRepairChipped = state.is(Blocks.CHIPPED_ANVIL);

        if (!canRepairDamaged && !canRepairChipped) {
            return InteractionResult.PASS;
        }

        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockState newState = (canRepairDamaged ? Blocks.CHIPPED_ANVIL : Blocks.ANVIL).defaultBlockState()
                .setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));

        world.setBlockAndUpdate(pos, newState);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
        world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        player.getItemInHand(hand).consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
