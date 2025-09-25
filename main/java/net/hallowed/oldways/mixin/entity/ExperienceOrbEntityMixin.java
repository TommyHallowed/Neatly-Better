package net.hallowed.oldways.mixin.entity;

import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.enchantment.MendingNerf;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;              // ⬅ add this
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Inject(method = "repairPlayerGears", at = @At("HEAD"), cancellable = true)
    private void hallowed$disableRepair(ServerPlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
        if (!CommonConfigManager.mendingNerfEnabled()) return;

        Optional<EnchantmentEffectContext> chosen =
                EnchantmentHelper.chooseEquipmentWith(EnchantmentEffectComponentTypes.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (chosen.isEmpty()) return;

        ItemStack stack = chosen.get().stack();
        ItemEnchantmentsComponent ench = EnchantmentHelper.getEnchantments(stack);

        // ⬇ call the new helper signature
        if (!MendingNerf.containsEnchant(ench, Enchantments.MENDING)) return;

        // Give XP to the player; do not repair via Mending
        cir.setReturnValue(amount);
        cir.cancel();
    }
}
