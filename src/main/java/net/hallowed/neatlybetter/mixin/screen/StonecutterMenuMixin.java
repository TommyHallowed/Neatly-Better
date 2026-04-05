package net.hallowed.neatlybetter.mixin.screen;

import net.hallowed.neatlybetter.util.StonecutterMemory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StonecutterMenu.class)
public class StonecutterMenuMixin {

    @Shadow @Final Slot resultSlot;

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void neatlybetter$recordQuickMoved(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        if (slotIndex == 1 && player instanceof StonecutterMemory memory) {
            ItemStack stack = this.resultSlot.getItem();
            if (!stack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                memory.neatlybetter$setLastCraftedItem(itemId);
            }
        }
    }
}