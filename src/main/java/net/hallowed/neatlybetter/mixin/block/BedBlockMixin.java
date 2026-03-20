package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.config.NTServerConfig.SleepMode;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.advancements.AdvancementHolder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class BedBlockMixin {

    @Inject(
            method = "useWithoutItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"
            ),
            cancellable = true
    )
    private void neatlybetter$preventSleep(
            BlockState blockState, Level level, BlockPos blockPos, Player player,
            BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir
    ) {
        SleepMode mode = NTServerConfig.CONFIG.allowSleeping.get();
        if (mode == SleepMode.ALLOW) return;
        if (!(player instanceof ServerPlayer sp)) return;

        if (mode == SleepMode.DRAGON) {
            AdvancementHolder advancement = sp.server.getAdvancements()
                    .get(Identifier.withDefaultNamespace("end/kill_dragon"));
            if (advancement != null
                    && sp.getAdvancements().getOrStartProgress(advancement).isDone()) {
                return;
            }
            sp.displayClientMessage(
                    Component.literal("You cannot sleep until Ender Dragon is Defeated"), true);
        }

        LevelData.RespawnData spawnPoint = LevelData.RespawnData.of(
                level.dimension(), blockPos, sp.getYRot(), sp.getXRot());
        sp.setRespawnPosition(new ServerPlayer.RespawnConfig(spawnPoint, false), true);

        cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
    }
}