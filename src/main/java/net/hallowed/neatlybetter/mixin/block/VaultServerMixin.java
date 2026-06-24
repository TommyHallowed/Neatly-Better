package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.data.VaultReopenData;
import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.block.entity.vault.VaultBlockEntity$Server")
public class VaultServerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private static void neatlybetter$processReopenExpirations(
            ServerLevel serverLevel, BlockPos pos, BlockState blockState,
            VaultConfig config, VaultServerData serverData, VaultSharedData sharedData,
            CallbackInfo ci
    ) {
        if (!NTServerConfig.CONFIG.vaultReopenEnabled.get()) return;
        if (serverLevel.getGameTime() % 20 != 0) return;

        BlockEntity be = serverLevel.getBlockEntity(pos);
        if (be == null) return;

        VaultReopenData data = be.getAttached(ModData.VAULT_REOPEN);
        if (data == null || data.isEmpty()) return;

        data.processExpirations(serverLevel.getGameTime(), uuid -> {
            serverData.rewardedPlayers.remove(uuid);

            serverData.isDirty = true;
        });
    }

    @WrapOperation(
            method = "tryInsertKey",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/vault/VaultServerData;addToRewardedPlayers(Lnet/minecraft/world/entity/player/Player;)V"
            )
    )
    private static void neatlybetter$recordReopenCooldown(
            VaultServerData serverData, Player player, Operation<Void> original,
            @Local(argsOnly = true, name = "serverLevel") ServerLevel serverLevel,
            @Local(argsOnly = true, name = "pos") BlockPos pos,
            @Local(argsOnly = true, name = "blockState") BlockState blockState
    ) {
        original.call(serverData, player);

        if (!NTServerConfig.CONFIG.vaultReopenEnabled.get()) return;

        BlockEntity be = serverLevel.getBlockEntity(pos);
        if (be == null) return;

        long cooldown = blockState.getValue(VaultBlock.OMINOUS)
                ? NTServerConfig.CONFIG.vaultOminousReopenCooldownTicks.get()
                : NTServerConfig.CONFIG.vaultReopenCooldownTicks.get();

        VaultReopenData data = be.getAttachedOrCreate(ModData.VAULT_REOPEN);
        data.record(player.getUUID(), serverLevel.getGameTime() + cooldown);
        be.setChanged();
    }
}