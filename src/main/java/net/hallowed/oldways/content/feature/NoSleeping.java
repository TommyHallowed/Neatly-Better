package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;

public final class NoSleeping {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!(world instanceof ServerLevel sw)) return InteractionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            BlockState state = sw.getBlockState(pos);
            if (!(state.getBlock() instanceof BedBlock)) return InteractionResult.PASS;
            if (sw.dimension() != Level.OVERWORLD) return InteractionResult.PASS;

            if (player instanceof ServerPlayer sp) {
                var advId = net.minecraft.resources.Identifier.withDefaultNamespace("end/kill_dragon");
                var advEntry = sw.getServer().getAdvancements().get(advId);
                boolean hasKillDragon = true;
                if (advEntry != null) {
                    hasKillDragon = sp.getAdvancements().getOrStartProgress(advEntry).isDone();
                }
                if (hasKillDragon) {
                    return InteractionResult.PASS;
                }

                LevelData.RespawnData spawnPoint =
                        LevelData.RespawnData.of(sw.dimension(), pos, sp.getYRot(), sp.getXRot());
                ServerPlayer.RespawnConfig respawn = new ServerPlayer.RespawnConfig(spawnPoint, false);
                sp.setRespawnPosition(respawn, true);
                sp.swing(hand, true);

                sp.displayClientMessage(net.minecraft.network.chat.Component.literal("You cannot sleep until ender dragon is defeated"), true);
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }

    private NoSleeping() {}
}
