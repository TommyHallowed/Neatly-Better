package net.hallowed.neatlybetter.mixin.item;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.handler.LegacyCombatHandler;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "finishUsingItem", at = @At("TAIL"))
    private void neatlybetter$foodAddEffects(Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack)(Object) this;
        if (livingEntity instanceof Player) {
            if (self.is(Items.GLISTERING_MELON_SLICE)
                    && livingEntity.getHealth() < 20.0F) {
                livingEntity.heal(2.0F);
            }
        }
    }

    @ModifyVariable(
            method = "postHurtEnemy",
            at = @At("STORE"),
            name = "weapon")
    private Weapon removeWeaponDurabilityPenalty(Weapon weapon) {
        if (!NTServerConfig.CONFIG.legacyCombat.get() || weapon == null) {
            return weapon;
        }
        if (weapon.itemDamagePerAttack() == 2) {
            return new Weapon(1, weapon.disableBlockingForSeconds());
        }
        return weapon;
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void neatlybetter$startSwordBlock(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (LegacyCombatHandler.isSword(self) && !player.isUsingItem()) {
            player.startUsingItem(hand);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$swordBlockUseDuration(LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (LegacyCombatHandler.isSword(self)) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$swordBlockUseAnimation(CallbackInfoReturnable<ItemUseAnimation> cir) {
        if (!NTServerConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (LegacyCombatHandler.isSword(self)) {
            cir.setReturnValue(ItemUseAnimation.BLOCK);
        }
    }
}
