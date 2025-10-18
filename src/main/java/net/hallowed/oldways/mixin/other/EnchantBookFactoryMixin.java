package net.hallowed.oldways.mixin.other;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.village.TradeOffers$EnchantBookFactory")
public class EnchantBookFactoryMixin {

    @ModifyArg(
            method = "create(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/village/TradeOffer;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentLevelEntry;<init>(Lnet/minecraft/registry/entry/RegistryEntry;I)V"
            ),
            index = 1
    )
    private int forceLevelOne(int originalLevel) {
        return 1;
    }
}
