package net.hallowed.oldways.mixin.shield;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the shield "raise" delay:
 * as soon as the player starts using a shield, treat it as blocking.
 * Mapping-safe: no component API or private field names.
 */
@Mixin(LivingEntity.class)
public abstract class NoShieldDelay {

    /**
     * 1.21.8: ItemStack LivingEntity#getBlockingItem()
     */
    @Inject(method = "getBlockingItem()Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void oldways$blockInstantly(CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!self.isUsingItem()) return;
        ItemStack active = self.getActiveItem();
        if (active.isEmpty() || !active.isOf(Items.SHIELD)) return;

        // Bypass vanilla delay checks: immediately report the active shield as blocking
        cir.setReturnValue(active);
    }
}
