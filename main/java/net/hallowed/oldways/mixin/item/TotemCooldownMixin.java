package net.hallowed.oldways.mixin.item;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class TotemCooldownMixin {

    @Inject(
            method = "tryUseDeathProtector(Lnet/minecraft/entity/damage/DamageSource;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hallowed$blockIfTotemCooling(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity player) {
            ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
            if (player.getItemCooldownManager().isCoolingDown(totem)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Inject(
            method = "tryUseDeathProtector(Lnet/minecraft/entity/damage/DamageSource;)Z",
            at = @At("RETURN")
    )
    private void hallowed$applyTotemCooldown(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && (Object) this instanceof PlayerEntity player) {
            int ticks = CommonConfigManager.totemCooldownTicks(); // <- use helper
            if (ticks > 0) {
                player.getItemCooldownManager().set(new ItemStack(Items.TOTEM_OF_UNDYING), ticks);
            }
        }
    }
}
