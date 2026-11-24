package net.hallowed.neatlybetter.feature.sleep;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.config.NTServerConfig.SleepMode;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelData;

public final class SleepModeHandler {
    private SleepModeHandler() {}

    public static void register() {
        // One listener for all sleep rules; the first rule that returns a problem wins.
        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            Player.BedSleepingProblem problem = sleepModeRule(player, sleepingPos);
            return problem != null ? problem : bedColorRule(player, sleepingPos);
        });
    }

    /** Server config {@code allowSleeping}: never, or only after the dragon (sets the respawn point either way). */
    private static Player.BedSleepingProblem sleepModeRule(Player player, BlockPos sleepingPos) {
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
    }

    /** Holding a dye: you can only sleep in a bed of that color. */
    private static Player.BedSleepingProblem bedColorRule(Player player, BlockPos sleepingPos) {
        if (!(player.level().getBlockState(sleepingPos).getBlock() instanceof BedBlock bed)) return null;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof DyeItem) {
                DyeColor heldDye = stack.get(DataComponents.DYE);
                if (heldDye == null) return null;
                return heldDye != bed.getColor() ? Player.BedSleepingProblem.OTHER_PROBLEM : null;
            }
        }
        return null;
    }
}
