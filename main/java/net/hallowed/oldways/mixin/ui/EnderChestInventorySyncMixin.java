package net.hallowed.oldways.mixin.ui;

import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Push ender state when the Ender Chest is opened/closed. */
@Mixin(EnderChestInventory.class)
public abstract class EnderChestInventorySyncMixin {

    @Inject(method = "onOpen", at = @At("TAIL"))
    private void oldways$pushOnOpen(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity sp) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }

    @Inject(method = "onClose", at = @At("TAIL"))
    private void oldways$pushOnClose(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity sp) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }
}