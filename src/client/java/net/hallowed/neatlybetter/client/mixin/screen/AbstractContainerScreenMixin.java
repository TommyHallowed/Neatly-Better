package net.hallowed.neatlybetter.client.mixin.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow protected Slot hoveredSlot;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$onRightClickArmorSwap(
            MouseButtonEvent mouseButtonEvent, boolean bl,
            CallbackInfoReturnable<Boolean> cir) {

        if (mouseButtonEvent.button() != 1) return;
        if (this.hoveredSlot == null || !this.hoveredSlot.hasItem()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (!player.containerMenu.getCarried().isEmpty()) return;

        if ((Object) this instanceof CreativeModeInventoryScreen) return;

        ItemStack clickedItem = this.hoveredSlot.getItem();
        Equippable equippable = clickedItem.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return;

        EquipmentSlot eqSlot = equippable.slot();
        if (eqSlot != EquipmentSlot.HEAD && eqSlot != EquipmentSlot.CHEST &&
                eqSlot != EquipmentSlot.LEGS && eqSlot != EquipmentSlot.FEET) {
            return;
        }

        if (this.hoveredSlot.container == player.getInventory()) {
            int containerSlot = this.hoveredSlot.getContainerSlot();
            if (containerSlot >= 36 && containerSlot <= 39) return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        net.hallowed.neatlybetter.client.network.NTNetworkClient.sendArmorSwap(
                screen.getMenu().containerId,
                this.hoveredSlot.index);
        cir.setReturnValue(true);
    }
}
