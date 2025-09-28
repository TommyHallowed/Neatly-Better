package net.hallowed.oldways.mixin.block;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

        if (!CommonConfigManager.bedNerfEnabled()) return;
        if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return;

        // 🔒 Only in Overworld
        if (world.getRegistryKey() != World.OVERWORLD) return;

        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        AdvancementEntry entry = server.getAdvancementLoader()
                .get(Identifier.of("minecraft", "end/kill_dragon"));
        if (entry == null) return;

        PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();
        AdvancementProgress progress = tracker.getProgress(entry);

        if (!progress.isDone()) {
            // Feedback
            serverPlayer.swingHand(Hand.MAIN_HAND, true);
            serverPlayer.sendMessage(Text.literal("You cannot rest until the Ender Dragon is defeated"), true);

            // Set spawn point without allowing sleep
            ServerPlayerEntity.Respawn respawn = new ServerPlayerEntity.Respawn(
                    world.getRegistryKey(),
                    pos,
                    serverPlayer.getYaw(),
                    true
            );
            serverPlayer.setSpawnPoint(respawn, true);

            // Block the actual sleep UI
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
