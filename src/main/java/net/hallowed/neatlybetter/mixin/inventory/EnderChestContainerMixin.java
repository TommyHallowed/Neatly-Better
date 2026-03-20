package net.hallowed.neatlybetter.mixin.inventory;

import net.hallowed.neatlybetter.network.NTNetwork;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Push ender state when the Ender Chest is opened/closed. */
@Mixin(PlayerEnderChestContainer.class)
public abstract class EnderChestContainerMixin {

    @Inject(method = "startOpen", at = @At("TAIL"))
    private void neatlybetter$pushOnOpen(ContainerUser user, CallbackInfo ci) {
        if (user instanceof ServerPlayer sp) {
            NTNetwork.pushEnderChestState(sp);
        }
    }

    @Inject(method = "stopOpen", at = @At("TAIL"))
    private void neatlybetter$pushOnClose(ContainerUser user, CallbackInfo ci) {
        if (user instanceof ServerPlayer sp) {
            NTNetwork.pushEnderChestState(sp);
        }
    }
}
