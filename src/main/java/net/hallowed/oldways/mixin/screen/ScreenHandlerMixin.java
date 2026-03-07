package net.hallowed.oldways.mixin.screen;

import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin {

    @Inject(method = "clicked", at = @At("TAIL"))
    private void oldways$enderSyncAfterClick(int slotIndex, int button, ClickType actionType,
                                             Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer sp)) return;
        if (!((Object) this instanceof ChestMenu g)) return;

        Container inv = g.getContainer();
        if (inv == sp.getEnderChestInventory()) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void oldways$enderSyncOnClose(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer sp)) return;
        if (!((Object) this instanceof ChestMenu g)) return;

        Container inv = g.getContainer();
        if (inv == sp.getEnderChestInventory()) {
            OldWaysNetwork.pushEnderChestState(sp);
        }
    }
}
