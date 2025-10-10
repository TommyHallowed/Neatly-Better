package net.hallowed.oldways.mixin.block;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {

    @Inject(
            method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$noSleepButSetSpawnWithRule(BlockState state,
                                                    World world,
                                                    BlockPos pos,
                                                    PlayerEntity player,
                                                    BlockHitResult hit,
                                                    CallbackInfoReturnable<ActionResult> cir) {
        if (!(world instanceof ServerWorld sw)) return;

        if (sw.getGameRules().getBoolean(ModGameRules.ALLOW_SLEEP)) return;

        if (world.getRegistryKey() != World.OVERWORLD) return;

        if (player instanceof ServerPlayerEntity sp) {
            sp.swingHand(Hand.MAIN_HAND, true);
            ServerPlayerEntity.Respawn respawn = new ServerPlayerEntity.Respawn(
                    sw.getRegistryKey(),
                    pos,
                    sp.getYaw(),
                    true
            );
            sp.setSpawnPoint(respawn, true);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
