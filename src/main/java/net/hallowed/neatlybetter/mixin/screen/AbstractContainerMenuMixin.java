package net.hallowed.neatlybetter.mixin.screen;

import net.hallowed.neatlybetter.network.NTNetwork;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;

import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Inject(method = "clicked", at = @At("TAIL"))
    private void neatlybetter$enderSyncAfterClick(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer sp)) return;
        if (!((Object) this instanceof ChestMenu g)) return;

        Container inv = g.getContainer();
        if (inv == sp.getEnderChestInventory()) {
            NTNetwork.pushEnderChestState(sp);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void neatlybetter$enderSyncOnClose(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer sp)) return;
        if (!((Object) this instanceof ChestMenu g)) return;

        Container inv = g.getContainer();
        if (inv == sp.getEnderChestInventory()) {
            NTNetwork.pushEnderChestState(sp);
        }
    }
}
