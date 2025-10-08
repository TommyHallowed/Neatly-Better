package net.hallowed.oldways.mixin.screen;

import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.util.OldEnchantCostContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Shadow @Final public int[] enchantmentPower;

    @Inject(method = "onButtonClick", at = @At("HEAD"))
    private void oldways$capture(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!CommonConfigManager.oldEnchant()) return;
        if (id >= 0 && id < this.enchantmentPower.length) {
            int required = Math.max(1, this.enchantmentPower[id]);
            OldEnchantCostContext.push(required, player.experienceLevel);
        }
    }

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void oldways$apply(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!CommonConfigManager.oldEnchant()) return;
            if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
            Integer required = OldEnchantCostContext.peekRequired();
            Integer startLvl = OldEnchantCostContext.peekStartLevel();
            if (required == null || startLvl == null) return;
            int target = Math.max(0, startLvl - required);
            int delta = target - player.experienceLevel;
            if (delta != 0) player.addExperienceLevels(delta);
        } finally {
            OldEnchantCostContext.clear();
        }
    }
}
