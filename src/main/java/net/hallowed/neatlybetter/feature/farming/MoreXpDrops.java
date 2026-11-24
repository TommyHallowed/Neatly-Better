package net.hallowed.neatlybetter.feature.farming;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.data.PlayerPlacedBlockData;
import net.hallowed.neatlybetter.util.CropUtil;
import net.hallowed.neatlybetter.util.ServerTaskQueue;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;


public final class MoreXpDrops {
    private MoreXpDrops() {}


    public static void register() {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world instanceof ServerLevel serverLevel
                    && player instanceof ServerPlayer serverPlayer
                    && !player.isSpectator()
                    && player.getItemInHand(hand).getItem() instanceof BlockItem) {

                BlockPos clicked = hitResult.getBlockPos().immutable();
                BlockPos offset  = clicked.relative(hitResult.getDirection());

                BlockState clickedBefore = serverLevel.getBlockState(clicked);
                BlockState offsetBefore  = serverLevel.getBlockState(offset);
                // Compare after the vanilla placement has run
                ServerTaskQueue.runAtEndOfTick(() ->
                        onPlacementSettled(serverPlayer, serverLevel, clicked, clickedBefore, offset, offsetBefore));
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, _) -> {
            if (!(world instanceof ServerLevel serverLevel)) return;
            if (player.isCreative()) return;

            if (CropUtil.isMature(state)) {
                tryAwardCropXp(serverLevel, pos);
                return;
            }

            if (!NTServerConfig.CONFIG.xpFromMiningNonOre.get()) return;
            if (state.getBlock() instanceof DropExperienceBlock) return;

            PlayerPlacedBlockData data = PlayerPlacedBlockData.get(serverLevel);
            if (data.remove(pos)) return;

            float hardness = state.getDestroySpeed(world, pos);
            if (hardness <= 0) return;

            double baseChance = 0.01;
            double maxChance  = 0.4;
            double chance = Math.min(baseChance + hardness * 0.01, maxChance);

            if (serverLevel.getRandom().nextDouble() >= chance) return;

            double multiplier = 0.15;
            int xp = Math.max(1, (int) (multiplier * hardness));

            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), xp);
        });
    }

    public static void tryAwardCropXp(ServerLevel level, BlockPos pos) {
        if (!NTServerConfig.CONFIG.xpFromFarming.get()) return;

        if (level.getRandom().nextDouble() >= 0.2) return;

        int xp = 2;
        ExperienceOrb.award(level, Vec3.atCenterOf(pos), xp);
    }


    private static void onPlacementSettled(ServerPlayer player, ServerLevel level,
                                           BlockPos pos1, BlockState state1,
                                           BlockPos pos2, BlockState state2) {
        boolean placed = processPosition(level, pos1, state1)
                || processPosition(level, pos2, state2);

        if (placed
                && !player.isCreative()
                && NTServerConfig.CONFIG.xpFromPlacingBlocks.get()) {
            double chance = 0.01;
            if (level.getRandom().nextDouble() < chance) {
                int xp = 1;
                ExperienceOrb.award(level, player.position(), xp);
            }
        }
    }

    private static boolean processPosition(ServerLevel level, BlockPos pos, BlockState stateBefore) {
        BlockState stateNow = level.getBlockState(pos);

        if (stateNow.equals(stateBefore) || stateNow.isAir()) return false;

        if (!(stateNow.getBlock() instanceof DropExperienceBlock)
                && stateNow.getDestroySpeed(level, pos) > 0) {
            PlayerPlacedBlockData.get(level).add(pos);
        }

        return true;
    }
}