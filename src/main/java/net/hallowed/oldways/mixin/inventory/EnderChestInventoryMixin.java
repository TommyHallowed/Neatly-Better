package net.hallowed.oldways.mixin.inventory;

import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Push ender state when the Ender Chest is opened/closed. */
@Mixin(PlayerEnderChestContainer.class)
public abstract class EnderChestInventoryMixin {

    @Inject(method = "startOpen", at = @At("TAIL"))
    private void oldways$pushOnOpen(ContainerUser user, CallbackInfo ci) {
        if (user instanceof ServerPlayer sp) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }

    @Inject(method = "stopOpen", at = @At("TAIL"))
    private void oldways$pushOnClose(ContainerUser user, CallbackInfo ci) {
        if (user instanceof ServerPlayer sp) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }
}
