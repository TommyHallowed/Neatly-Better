package net.hallowed.oldways.client.mixin.screen;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
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
    private void oldways$onRightClickArmorSwap(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (mouseButtonEvent.button() != 1 || this.hoveredSlot == null || !this.hoveredSlot.hasItem()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.gameMode == null || !player.containerMenu.getCarried().isEmpty()) return;

        if ((Object) this instanceof CreativeModeInventoryScreen) return;

        ItemStack clickedItem = this.hoveredSlot.getItem();

        Equippable equippable = clickedItem.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return;

        EquipmentSlot equipmentSlot = equippable.slot();

        if (equipmentSlot != EquipmentSlot.HEAD &&
                equipmentSlot != EquipmentSlot.CHEST &&
                equipmentSlot != EquipmentSlot.LEGS &&
                equipmentSlot != EquipmentSlot.FEET) {
            return;
        }

        int inventoryIndexTarget = -1;
        switch (equipmentSlot) {
            case HEAD -> inventoryIndexTarget = 39;
            case CHEST -> inventoryIndexTarget = 38;
            case LEGS -> inventoryIndexTarget = 37;
            case FEET -> inventoryIndexTarget = 36;
            default -> {}
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        int menuEquipmentSlotId = -1;

        for (Slot slot : screen.getMenu().slots) {
            if (slot.container == player.getInventory() && slot.getContainerSlot() == inventoryIndexTarget) {
                menuEquipmentSlotId = slot.index;
                break;
            }
        }

        if (menuEquipmentSlotId != -1) {
            ItemStack equippedItem = player.getItemBySlot(equipmentSlot);
            int containerId = screen.getMenu().containerId;
            int clickedSlotId = this.hoveredSlot.index;

            if (equippedItem.isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(containerId, clickedSlotId, 0, ClickType.PICKUP, player);
                mc.gameMode.handleInventoryMouseClick(containerId, menuEquipmentSlotId, 0, ClickType.PICKUP, player);
                cir.setReturnValue(true);
            } else if (equippedItem != clickedItem) {
                mc.gameMode.handleInventoryMouseClick(containerId, clickedSlotId, 0, ClickType.PICKUP, player);
                mc.gameMode.handleInventoryMouseClick(containerId, menuEquipmentSlotId, 0, ClickType.PICKUP, player);
                mc.gameMode.handleInventoryMouseClick(containerId, clickedSlotId, 0, ClickType.PICKUP, player);
                cir.setReturnValue(true);
            }
        }
    }
}