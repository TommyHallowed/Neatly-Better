package net.hallowed.oldways.mixin.screen;

import net.hallowed.oldways.util.StonecutterMemory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.inventory.StonecutterMenu$2")
public class StonecutterScreenHandlerMixin {

    // This targets the specific anonymous Slot class for the output in StonecutterScreenHandler
    @Inject(method = "onTake", at = @At("HEAD"))
    private void oldways$recordCraftedItem(Player player, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && player instanceof StonecutterMemory memory) {
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            memory.oldways$setLastCraftedItem(itemId);
        }
    }
}