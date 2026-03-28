package net.hallowed.neatlybetter.mixin.entity.hostile;

import net.hallowed.neatlybetter.init.ModPotions;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Witch.class)
public abstract class WitchMixin {

    @Redirect(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack neatlybetter$useShortPoison(Item item, Holder<Potion> potion) {
        if (potion == Potions.POISON) {
            potion = ModPotions.SHORT_POISON;
        }
        return PotionContents.createItemStack(item, potion);
    }
}
