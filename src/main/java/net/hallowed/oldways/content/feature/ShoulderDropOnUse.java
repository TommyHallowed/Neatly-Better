package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

public final class ShoulderDropOnUse {
    private ShoulderDropOnUse() {}

    public static void register() {
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hit) -> {
            if (!(world instanceof ServerWorld)) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!player.getMainHandStack().isEmpty()) return ActionResult.PASS;
            if (!player.isInSneakingPose()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            boolean hasLeft  = !sp.getLeftShoulderNbt().isEmpty();
            boolean hasRight = !sp.getRightShoulderNbt().isEmpty();
            if (!hasLeft && !hasRight) return ActionResult.PASS;
            sp.dropShoulderEntities();
            return ActionResult.SUCCESS_SERVER;
        });
    }
}
