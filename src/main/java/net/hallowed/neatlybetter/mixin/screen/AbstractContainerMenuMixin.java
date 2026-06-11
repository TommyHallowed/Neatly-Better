package net.hallowed.neatlybetter.mixin.screen;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.network.NTNetwork;

import net.hallowed.neatlybetter.util.LapisUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;

import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
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

    @Inject(
            method = "clearContainer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/Container;)V",
            at = @At("HEAD")
    )
    private void neatlybetter$saveLapisOnClose(Player player, Container container, CallbackInfo ci) {
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        if (!(self instanceof EnchantmentMenu)) return;
        if (!NTServerConfig.CONFIG.lapisStaysInEnchanting.get()) return;

        EnchantingTableBlockEntity enchTable = LapisUtil.getEnchantingTableBlockEntity(player);
        if (enchTable == null) return;

        ItemStack lapisStack = container.getItem(1);
        int lapisCount = lapisStack.is(Items.LAPIS_LAZULI) ? lapisStack.getCount() : 0;

        LapisUtil.saveLapisCount(player.level(), enchTable, lapisCount);

        container.setItem(1, ItemStack.EMPTY);
    }
}
