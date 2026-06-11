package net.hallowed.neatlybetter.mixin.entity.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Shadow
    public AbstractArrow.Pickup pickup;

    @Shadow
    private @Nullable ItemStack firedFromWeapon;

    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$allowInfinityArrowPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.pickup == AbstractArrow.Pickup.CREATIVE_ONLY && neatlybetter$hasInfinity(this.firedFromWeapon)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean neatlybetter$hasInfinity(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemEnchantments ench = stack.getOrDefault(
                DataComponents.ENCHANTMENTS,
                ItemEnchantments.EMPTY
        );
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : ench.entrySet()) {
            if (e.getKey().is(Enchantments.INFINITY) && e.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }
}
