package net.hallowed.neatlybetter.mixin.item;

import net.hallowed.neatlybetter.config.NTCommonConfig;
import net.hallowed.neatlybetter.handler.LegacyCombatHandler;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract boolean is(Predicate<Holder<Item>> item);

    @Shadow
    public abstract Item getItem();

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
        if (!NTCommonConfig.CONFIG.legacyCombat.get() || weapon == null) {
            return weapon;
        }
        if (weapon.itemDamagePerAttack() == 2) {
            return new Weapon(1, weapon.disableBlockingForSeconds());
        }
        return weapon;
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void neatlybetter$startSwordBlock(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!NTCommonConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        if (player.getOffhandItem().getItem() instanceof ShieldItem) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (LegacyCombatHandler.isSword(self) && !player.isUsingItem()) {
            player.startUsingItem(hand);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$swordBlockUseDuration(LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (!NTCommonConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        if (user.getOffhandItem().getItem() instanceof ShieldItem) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (LegacyCombatHandler.isSword(self)) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$swordBlockUseAnimation(CallbackInfoReturnable<ItemUseAnimation> cir) {
        if (!NTCommonConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (LegacyCombatHandler.isSword(self)) {
            cir.setReturnValue(ItemUseAnimation.BLOCK);
        }
    }
}
