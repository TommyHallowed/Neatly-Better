package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.config.NTServerConfig.SleepMode;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelData;

public class SleepModeHandler {
    private SleepModeHandler() {}

    public static void register() {
        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            SleepMode mode = NTServerConfig.CONFIG.allowSleeping.get();
            if (mode == SleepMode.ALLOW) return null;
            if (!(player instanceof ServerPlayer sp)) return null;

            if (mode == SleepMode.DRAGON) {
                AdvancementHolder advancement = sp.server.getAdvancements()
                        .get(Identifier.withDefaultNamespace("end/kill_dragon"));
                if (advancement != null
                        && sp.getAdvancements().getOrStartProgress(advancement).isDone()) {
                    return null;
                }
                sp.sendSystemMessage(
                        Component.literal("You cannot sleep until Ender Dragon is Defeated"), true);
            }

            LevelData.RespawnData spawnPoint = LevelData.RespawnData.of(
                    sp.level().dimension(), sleepingPos, sp.getYRot(), sp.getXRot());
            sp.setRespawnPosition(new ServerPlayer.RespawnConfig(spawnPoint, false), true);

            return Player.BedSleepingProblem.OTHER_PROBLEM;
        });
    }
}
