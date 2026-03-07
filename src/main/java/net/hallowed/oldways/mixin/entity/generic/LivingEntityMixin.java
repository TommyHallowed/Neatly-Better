package net.hallowed.oldways.mixin.entity.generic;

import net.hallowed.oldways.init.ModGameRules;
import net.hallowed.oldways.util.ProtectionContext;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /* ===================== 1) Totem cooldown ===================== */

    @Inject(
            method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$blockIfTotemCooling(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Player player) {
            ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
            if (player.getCooldowns().isOnCooldown(totem)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Inject(
            method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At("RETURN")
    )
    private void oldways$applyTotemCooldown(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Player player) {
                player.getCooldowns().addCooldown(new ItemStack(Items.TOTEM_OF_UNDYING), 1200);
        }
    }

    /* ===================== 2) Protection context ===================== */

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("HEAD"))
    private void oldways$setProtContext(DamageSource source, float amount,
                                        CallbackInfoReturnable<Float> cir) {
        ProtectionContext.set((LivingEntity)(Object)this, source);
    }

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
    private void oldways$clearProtContext(DamageSource source, float amount,
                                          CallbackInfoReturnable<Float> cir) {
        ProtectionContext.clear();
    }

    /* ===================== 3) Resistance effect absorption nerf ===================== */

    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(intValue = 25))
    private int oldways$resistanceDenominatorInt(int original) {
        return 50;
    }

    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(floatValue = 25.0F))
    private float oldways$resistanceDenominatorFloat(float original) {
        return 50.0F;
    }

    /* ===================== 4) Explosions disable shields ===================== */

    @Inject(
            method = "applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At("RETURN")
    )
    private void oldways$explosionDisablesShield(ServerLevel world,
                                                 DamageSource source,
                                                 float amount,
                                                 CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() <= 0.0F) return;
        if (!source.is(DamageTypeTags.IS_EXPLOSION)) return;

        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Player player)) return;

        ItemStack blocking = self.getItemBlockingWith();
        if (blocking == null || blocking.isEmpty()) return;

        BlocksAttacks blocks = blocking.get(DataComponents.BLOCKS_ATTACKS);
        if (blocks == null) return;

        blocks.disable(world, player, 5.0F, blocking);
        self.releaseUsingItem();
    }

    /* ===================== 5) No shield raise delay ===================== */

    @Inject(method = "getItemBlockingWith()Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void oldways$customShieldRaiseDelay(CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!self.isUsingItem()) return;
        ItemStack active = self.getUseItem();
        if (active.isEmpty() || !active.is(Items.SHIELD)) return;

        int delay = 0;
        Level w = self.level();
        if (w instanceof ServerLevel sw) {
            delay = Math.max(0, sw.getGameRules().get(ModGameRules.SHIELD_RAISE_DELAY_TICKS));
        }

        if (delay <= 0 || self.getTicksUsingItem() >= delay) {
            cir.setReturnValue(active);
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
        cir.cancel();
    }
}
