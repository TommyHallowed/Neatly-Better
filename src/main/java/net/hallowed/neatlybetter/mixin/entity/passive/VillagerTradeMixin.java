package net.hallowed.neatlybetter.mixin.entity.passive;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTrade.class)
public class VillagerTradeMixin {

    @Inject(method = "getOffer", at = @At("RETURN"))
    private void neatlybetter$capBookLevel(LootContext lootContext, CallbackInfoReturnable<MerchantOffer> cir) {
        if (!NTServerConfig.CONFIG.villagerBookLevelCap.get()) return;

        MerchantOffer offer = cir.getReturnValue();
        if (offer == null) return;

        ItemStack result = offer.getResult();
        if (result.getItem() != Items.ENCHANTED_BOOK) return;

        ItemEnchantments stored = result.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored == null || stored.isEmpty()) return;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Holder<Enchantment> enchantment : stored.keySet()) {
            mutable.set(enchantment, 1);
        }
        result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
    }
}
