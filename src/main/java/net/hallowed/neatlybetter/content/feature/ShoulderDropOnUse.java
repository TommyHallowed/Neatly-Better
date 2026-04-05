package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class ShoulderDropOnUse {
    private ShoulderDropOnUse() {}

    public static void register() {
        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult _) -> {
            if (!(world instanceof ServerLevel)) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
            if (!player.isCrouching()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            boolean hasLeft  = !sp.getShoulderEntityLeft().isEmpty();
            boolean hasRight = !sp.getShoulderEntityRight().isEmpty();
            if (!hasLeft && !hasRight) return InteractionResult.PASS;
            sp.removeEntitiesOnShoulder();
            return InteractionResult.SUCCESS_SERVER;
        });
    }
}
