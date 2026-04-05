package net.hallowed.neatlybetter.mixin.screen;

import net.hallowed.neatlybetter.util.StonecutterMemory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.inventory.StonecutterMenu$2")
public class StonecutterMenu$2Mixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void neatlybetter$recordCraftedItem(Player player, ItemStack carried, CallbackInfo ci) {
        if (!carried.isEmpty() && player instanceof StonecutterMemory memory) {
            String itemId = BuiltInRegistries.ITEM.getKey(carried.getItem()).toString();
            memory.neatlybetter$setLastCraftedItem(itemId);
        }
    }
}