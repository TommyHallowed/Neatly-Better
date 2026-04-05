package net.hallowed.neatlybetter.mixin.item;

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
    private void neatlybetter$foodAddEffects(Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack)(Object) this;
        if (livingEntity instanceof Player) {
            if (self.is(Items.GLISTERING_MELON_SLICE)
                    && livingEntity.getHealth() < 20.0F) {
                livingEntity.heal(1.0F);
            }
        }
    }
}
