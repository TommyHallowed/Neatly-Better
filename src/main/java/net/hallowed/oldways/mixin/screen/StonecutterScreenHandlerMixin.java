package net.hallowed.oldways.mixin.screen;

import net.hallowed.oldways.util.StonecutterMemory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.screen.StonecutterScreenHandler$2")
public class StonecutterScreenHandlerMixin {

    // This targets the specific anonymous Slot class for the output in StonecutterScreenHandler
    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void oldways$recordCraftedItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && player instanceof StonecutterMemory memory) {
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            memory.oldways$setLastCraftedItem(itemId);
        }
    }
}