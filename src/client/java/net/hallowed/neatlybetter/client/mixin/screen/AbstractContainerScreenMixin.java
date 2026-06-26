package net.hallowed.neatlybetter.client.mixin.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

import net.hallowed.neatlybetter.client.network.NTNetworkClient;
import net.hallowed.neatlybetter.content.component.QuiverContents;
import net.hallowed.neatlybetter.content.item.QuiverItem;
import net.hallowed.neatlybetter.init.ModData;
import net.hallowed.neatlybetter.init.ModItems;

import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow protected Slot hoveredSlot;

    @Unique
    private final ScrollWheelHandler neatlybetter$quiverScroll = new ScrollWheelHandler();

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$onQuiverScroll(
            double x, double y, double scrollX, double scrollY,
            CallbackInfoReturnable<Boolean> cir) {

        Slot slot = this.hoveredSlot;
        if (slot == null || !slot.hasItem()) return;

        ItemStack stack = slot.getItem();
        if (stack.getItem() != ModItems.QUIVER) return;

        int shown = stack.getOrDefault(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY).size();
        if (shown == 0) {
            cir.setReturnValue(true);
            return;
        }

        Vector2i wheelXY = this.neatlybetter$quiverScroll.onMouseScroll(scrollX, scrollY);
        int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
        if (wheel != 0) {
            int selected = QuiverItem.getSelectedItemIndex(stack);
            int updated = ScrollWheelHandler.getNextScrollWheelSelection(wheel, selected, shown);
            if (selected != updated && updated < shown) {
                QuiverItem.toggleSelectedItem(stack, updated);
                NTNetworkClient.sendQuiverSelect(slot.index, updated);
            }
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$onRightClickArmorSwap(
            MouseButtonEvent event, boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir) {

        if (event.button() != 1) return;
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
        NTNetworkClient.sendArmorSwap(
                screen.getMenu().containerId,
                this.hoveredSlot.index);
        cir.setReturnValue(true);
    }
}
