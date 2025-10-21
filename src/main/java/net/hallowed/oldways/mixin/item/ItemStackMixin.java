package net.hallowed.oldways.mixin.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
    private void oldways$foodAddEffects(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack)(Object) this;
        if (user instanceof PlayerEntity) {
            if (self.isOf(Items.GLISTERING_MELON_SLICE)
                    && user.getHealth() < 20.0F) {
                user.heal(1.0F);
            } else if (self.isOf(Items.GLOW_BERRIES)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0), user);
            }
        }
    }
}
