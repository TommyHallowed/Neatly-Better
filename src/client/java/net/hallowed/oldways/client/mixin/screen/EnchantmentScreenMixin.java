package net.hallowed.oldways.client.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Show the true XP level cost under each option and do NOT touch the lapis line.
 * We only rewrite:
 *   container.enchant.level.one
 *   container.enchant.level.many
 * based on the local 'k' (handler.enchantmentPower[j]).
 */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {

    // Intercept Text.translatable("container.enchant.level.one")
    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"
            )
    )
    private MutableText oldways$levelOne(
            String key,
            Operation<MutableText> original,
            // vanilla local: int k = this.handler.enchantmentPower[j];
            @Local(name = "k") int k
    ) {
        if (!"container.enchant.level.one".equals(key) || !CommonConfigManager.oldEnchant())  {
            return original.call(key);
        }
        int cost = Math.max(1, k);
        return (cost == 1)
                ? Text.translatable("container.enchant.level.one")
                : Text.translatable("container.enchant.level.many", cost);
    }

    // Intercept Text.translatable("container.enchant.level.many", ...)
    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            )
    )
    private MutableText oldways$levelMany(
            String key,
            Object[] args,
            Operation<MutableText> original,
            @Local(name = "k") int k
    ) {
        if (!"container.enchant.level.many".equals(key) || !CommonConfigManager.oldEnchant()) {
            return original.call(key, args);
        }
        int cost = Math.max(1, k);
        return (cost == 1)
                ? Text.translatable("container.enchant.level.one")
                : Text.translatable("container.enchant.level.many", cost);
    }
}
