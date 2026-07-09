package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.data.PlayerPlacedBlockData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MoreXpDrops {

    private record PendingPlacement(ServerPlayer player, ServerLevel level,
                                    BlockPos pos1, BlockState state1,
                                    BlockPos pos2, BlockState state2) {}

    private static final List<PendingPlacement> PENDING = new ArrayList<>();

    public static void init() {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world instanceof ServerLevel serverLevel
                    && player instanceof ServerPlayer serverPlayer
                    && !player.isSpectator()
                    && player.getItemInHand(hand).getItem() instanceof BlockItem) {

                BlockPos clicked = hitResult.getBlockPos().immutable();
                BlockPos offset  = clicked.relative(hitResult.getDirection());

                PENDING.add(new PendingPlacement(
                        serverPlayer,
                        serverLevel,
                        clicked, serverLevel.getBlockState(clicked),
                        offset,  serverLevel.getBlockState(offset)
                ));
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(_ -> {
            for (PendingPlacement p : PENDING) {
                boolean placed = processPosition(p.level, p.pos1, p.state1)
                        || processPosition(p.level, p.pos2, p.state2);

                if (placed
                        && !p.player.isCreative()
                        && NTServerConfig.CONFIG.xpFromPlacingBlocks.get()) {
                    double chance = 0.01;
                    if (p.level.getRandom().nextDouble() < chance) {
                        int xp = 1;
                        ExperienceOrb.award(p.level, p.player.position(), xp);
                    }
                }
            }
            PENDING.clear();
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, _) -> {
            if (!(world instanceof ServerLevel serverLevel)) return;
            if (player.isCreative()) return;

            if (isMatureCrop(state)) {
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

    public static boolean isMatureCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        return false;
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