package net.hallowed.oldways.mixin.inventory;

import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.entity.ContainerUser;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Push ender state when the Ender Chest is opened/closed. */
@Mixin(EnderChestInventory.class)
public abstract class EnderChestInventoryMixin {

    @Inject(method = "onOpen", at = @At("TAIL"))
    private void oldways$pushOnOpen(ContainerUser user, CallbackInfo ci) {
        if (user instanceof ServerPlayerEntity sp) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }

    @Inject(method = "onClose", at = @At("TAIL"))
    private void oldways$pushOnClose(ContainerUser user, CallbackInfo ci) {
        if (user instanceof ServerPlayerEntity sp) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }
}
