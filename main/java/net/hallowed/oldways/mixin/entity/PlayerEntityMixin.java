package net.hallowed.oldways.mixin.entity;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getProjectileType", at = @At("RETURN"), cancellable = true)
    private void hallowed$virtualArrowForInfinity(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        if (!CommonConfigManager.infinityFixEnabled()) return;
        // vanilla already found a projectile?
        if (!cir.getReturnValue().isEmpty()) return;

        if (!(weapon.getItem() instanceof BowItem)) return;

        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getAbilities().creativeMode) return;

        // 1.21.8: resolve RegistryEntry via lookup -> getOrThrow(key)
        var enchLookup = self.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> infinity = enchLookup.getOrThrow(Enchantments.INFINITY);

        if (EnchantmentHelper.getLevel(infinity, weapon) <= 0) return;

        // pretend we have a normal arrow so shooting is allowed
        cir.setReturnValue(new ItemStack(Items.ARROW));
    }
}
