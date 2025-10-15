package net.hallowed.oldways.mixin.screen;

import net.hallowed.oldways.mixin.accessor.GenericContainerScreenHandlerAccessor;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Inject(method = "onSlotClick", at = @At("TAIL"))
    private void oldways$enderSyncAfterClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!((Object) this instanceof GenericContainerScreenHandler g)) return;

        Inventory inv = ((GenericContainerScreenHandlerAccessor) g).oldways$getInventory();
        if (inv == sp.getEnderChestInventory()) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }

    @Inject(method = "onClosed", at = @At("TAIL"))
    private void oldways$enderSyncOnClose(PlayerEntity player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!((Object) this instanceof GenericContainerScreenHandler g)) return;

        Inventory inv = ((GenericContainerScreenHandlerAccessor) g).oldways$getInventory();
        if (inv == sp.getEnderChestInventory()) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }
}
