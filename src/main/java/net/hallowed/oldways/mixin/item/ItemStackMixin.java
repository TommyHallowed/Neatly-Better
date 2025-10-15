package net.hallowed.oldways.mixin.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "finishUsing", at = @At("TAIL"))
    private void oldways$glisteringMelonHealsOneHp(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack)(Object) this;
        if (!world.isClient()
                && self.isOf(Items.GLISTERING_MELON_SLICE)
                && user instanceof PlayerEntity
                && user.getHealth() < 20.0F) {
            user.heal(1.0F);
        }
    }
}
