package net.hallowed.oldways.mixin.block;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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
    private void oldways$denySleepUntilDragon(BlockState state,
                                              World world,
                                              BlockPos pos,
                                              PlayerEntity player,
                                              BlockHitResult hit,
                                              CallbackInfoReturnable<ActionResult> cir) {

        if (!(world instanceof ServerWorld sw)) return;
        if (!sw.getGameRules().getBoolean(ModGameRules.ALLOW_SLEEP_AFTER_ENDER_DRAGON_KILL)) return;
        if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return;

        if (world.getRegistryKey() != World.OVERWORLD) return;
        if (world.isDay()) return;
        if (!sw.isSleepingEnabled()) return;
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        AdvancementEntry entry = server.getAdvancementLoader()
                .get(Identifier.of("minecraft", "end/kill_dragon"));
        if (entry == null) return;

        PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();
        AdvancementProgress progress = tracker.getProgress(entry);

        if (!progress.isDone()) {
            serverPlayer.swingHand(Hand.MAIN_HAND, true);
            serverPlayer.sendMessage(Text.literal("You cannot rest until the Ender Dragon is defeated"), true);

            ServerPlayerEntity.Respawn respawn = new ServerPlayerEntity.Respawn(
                    world.getRegistryKey(),
                    pos,
                    serverPlayer.getYaw(),
                    true
            );
            serverPlayer.setSpawnPoint(respawn, true);

            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
