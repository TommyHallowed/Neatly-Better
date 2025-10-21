package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;

public final class NoSleeping {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!(world instanceof ServerWorld sw)) return ActionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            BlockState state = sw.getBlockState(pos);
            if (!(state.getBlock() instanceof BedBlock)) return ActionResult.PASS;
            if (sw.getRegistryKey() != World.OVERWORLD) return ActionResult.PASS;

            if (player instanceof ServerPlayerEntity sp) {
                var advId = net.minecraft.util.Identifier.ofVanilla("end/kill_dragon");
                var advEntry = sw.getServer().getAdvancementLoader().get(advId);
                boolean hasKillDragon = true;
                if (advEntry != null) {
                    hasKillDragon = sp.getAdvancementTracker().getProgress(advEntry).isDone();
                }
                if (hasKillDragon) {
                    return ActionResult.PASS;
                }

                WorldProperties.SpawnPoint spawnPoint =
                        WorldProperties.SpawnPoint.create(sw.getRegistryKey(), pos, sp.getYaw(), sp.getPitch());
                ServerPlayerEntity.Respawn respawn = new ServerPlayerEntity.Respawn(spawnPoint, false);
                sp.setSpawnPoint(respawn, true);
                sp.swingHand(hand, true);

                sp.sendMessage(net.minecraft.text.Text.literal("You cannot sleep until ender dragon is defeated"), true);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    private NoSleeping() {}
}
