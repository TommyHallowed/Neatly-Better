package net.hallowed.oldways.mixin.screen;

import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.enchant.OldEnchantCostContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Capture the full displayed level (enchantmentPower[id]) when a button is clicked.
 * We don't modify vanilla logic here—just stash the value for the actual charge.
 */
@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Shadow @Final public int[] enchantmentPower;

    @Inject(method = "onButtonClick", at = @At("HEAD"))
    private void oldways$captureCost(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        //Global Config Switch
        if (!CommonConfigManager.oldEnchant()) return;
        
        if (id >= 0 && id < this.enchantmentPower.length) {
            int full = Math.max(1, this.enchantmentPower[id]);
            OldEnchantCostContext.push(full);
        }
    }

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void oldways$clearCost(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        OldEnchantCostContext.clear();
    }
}
