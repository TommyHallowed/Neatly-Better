package net.hallowed.oldways.mixin.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "finishUsingItem", at = @At("TAIL"))
    private void oldways$foodAddEffects(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack)(Object) this;
        if (user instanceof Player) {
            if (self.is(Items.GLISTERING_MELON_SLICE)
                    && user.getHealth() < 20.0F) {
                user.heal(1.0F);
            } else if (self.is(Items.GLOW_BERRIES)) {
                user.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0), user);
            }
        }
    }
}
