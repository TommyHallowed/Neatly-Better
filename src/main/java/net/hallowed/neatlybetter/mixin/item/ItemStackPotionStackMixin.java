package net.hallowed.neatlybetter.mixin.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackPotionStackMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$potionsStack16(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack)(Object)this;
        Item it = self.getItem();
        if (it instanceof PotionItem || it instanceof SplashPotionItem || it instanceof LingeringPotionItem) {
            cir.setReturnValue(16);
        }
    }
}