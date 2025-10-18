package net.hallowed.oldways.mixin.entity.player;

import net.hallowed.oldways.content.feature.StructureProtection;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow protected ServerWorld world;
    @Final
    @Shadow protected ServerPlayerEntity player;

    @Shadow public abstract boolean isSurvivalLike();
    @Shadow public abstract boolean changeGameMode(GameMode gameMode);

    @Unique private static final Set<UUID> OLDWAYS_OUTSIDE_HOLD = ConcurrentHashMap.newKeySet();

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"), cancellable = true)
    private void oldways$preventStartAndHold(BlockPos pos,
                                             PlayerActionC2SPacket.Action action,
                                             Direction direction,
                                             int worldHeight,
                                             int sequence,
                                             CallbackInfo ci) {
        if (!this.isSurvivalLike()) return;

        if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            if (!StructureProtection.isLockedAndPlayerOutside(world, player, pos)) return;
            if (!oldways$isStructureCompositionBlock(world.getBlockState(pos).getBlock())) return;

            OLDWAYS_OUTSIDE_HOLD.add(player.getUuid());

            this.changeGameMode(GameMode.ADVENTURE);
            world.setBlockBreakingInfo(player.getId(), pos, -1);

            ci.cancel();
            return;
        }

        if (action == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                || action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {

            if (OLDWAYS_OUTSIDE_HOLD.remove(player.getUuid())) {
                world.setBlockBreakingInfo(player.getId(), pos, -1);
                if (player.interactionManager.getGameMode() == GameMode.ADVENTURE
                        && !StructureProtection.isLockedAndPlayerOutside(world, player, pos)) {
                    this.changeGameMode(GameMode.SURVIVAL);
                } else {
                    this.changeGameMode(GameMode.SURVIVAL);
                }
            }
        }
    }

    @Unique
    private static boolean oldways$isStructureCompositionBlock(Block block) {
        return block == Blocks.COBBLESTONE
                || block == Blocks.COBBLESTONE_SLAB
                || block == Blocks.COBBLESTONE_WALL
                || block == Blocks.COBBLESTONE_STAIRS
                || block == Blocks.MOSSY_COBBLESTONE
                || block == Blocks.DARK_OAK_LOG
                || block == Blocks.DARK_OAK_PLANKS
                || block == Blocks.DARK_OAK_STAIRS
                || block == Blocks.DARK_OAK_FENCE
                || block == Blocks.BIRCH_PLANKS
                || block == Blocks.GLASS_PANE
                || block == Blocks.WHITE_CARPET
                || block == Blocks.RED_CARPET
                || block == Blocks.TORCH
                || block == Blocks.NETHER_BRICKS
                || block == Blocks.NETHER_BRICK_FENCE
                || block == Blocks.SPAWNER
                || block == Blocks.TRIAL_SPAWNER;
    }
}
