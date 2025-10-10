package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class NoSleeping {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!(world instanceof ServerWorld sw)) return ActionResult.PASS;

            if (sw.getGameRules().getBoolean(ModGameRules.ALLOW_SLEEP)) return ActionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            BlockState state = sw.getBlockState(pos);

            if (!(state.getBlock() instanceof BedBlock)) return ActionResult.PASS;
            if (sw.getRegistryKey() != World.OVERWORLD) return ActionResult.PASS;

            if (player instanceof ServerPlayerEntity sp) {
                ServerPlayerEntity.Respawn respawn = new ServerPlayerEntity.Respawn(
                        sw.getRegistryKey(),
                        pos,
                        sp.getYaw(),
                        true
                );
                sp.setSpawnPoint(respawn, true);
                sp.swingHand(hand, true);
            }
            return ActionResult.SUCCESS;
        });
    }

    private NoSleeping() {}
}
