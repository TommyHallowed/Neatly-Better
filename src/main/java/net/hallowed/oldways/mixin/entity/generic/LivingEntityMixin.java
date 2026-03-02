package net.hallowed.oldways.mixin.entity.generic;
import net.hallowed.oldways.init.ModGameRules;
import net.hallowed.oldways.util.ProtectionContext;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;

import net.minecraft.world.World;
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
            method = "tryUseDeathProtector(Lnet/minecraft/entity/damage/DamageSource;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$blockIfTotemCooling(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof PlayerEntity player) {
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
    private void oldways$applyTotemCooldown(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof PlayerEntity player) {
                player.getItemCooldownManager().set(new ItemStack(Items.TOTEM_OF_UNDYING), 1200);
        }
    }

    /* ===================== 2) Protection context ===================== */

    @Inject(method = "modifyAppliedDamage", at = @At("HEAD"))
    private void oldways$setProtContext(DamageSource source, float amount,
                                        CallbackInfoReturnable<Float> cir) {
        ProtectionContext.set((LivingEntity)(Object)this, source);
    }

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"))
    private void oldways$clearProtContext(DamageSource source, float amount,
                                          CallbackInfoReturnable<Float> cir) {
        ProtectionContext.clear();
    }

    /* ===================== 3) Resistance effect absorption nerf ===================== */

    @ModifyConstant(method = "modifyAppliedDamage", constant = @Constant(intValue = 25))
    private int oldways$resistanceDenominatorInt(int original) {
        return 50;
    }

    @ModifyConstant(method = "modifyAppliedDamage", constant = @Constant(floatValue = 25.0F))
    private float oldways$resistanceDenominatorFloat(float original) {
        return 50.0F;
    }

    /* ===================== 4) Explosions disable shields ===================== */

    @Inject(
            method = "getDamageBlockedAmount(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)F",
            at = @At("RETURN")
    )
    private void oldways$explosionDisablesShield(ServerWorld world,
                                                 DamageSource source,
                                                 float amount,
                                                 CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() <= 0.0F) return;
        if (!source.isIn(DamageTypeTags.IS_EXPLOSION)) return;

        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof PlayerEntity player)) return;

        ItemStack blocking = self.getBlockingItem();
        if (blocking == null || blocking.isEmpty()) return;

        BlocksAttacksComponent blocks = blocking.get(DataComponentTypes.BLOCKS_ATTACKS);
        if (blocks == null) return;

        blocks.applyShieldCooldown(world, player, 5.0F, blocking);
        self.stopUsingItem();
    }

    /* ===================== 5) No shield raise delay ===================== */

    @Inject(method = "getBlockingItem()Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void oldways$customShieldRaiseDelay(CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!self.isUsingItem()) return;
        ItemStack active = self.getActiveItem();
        if (active.isEmpty() || !active.isOf(Items.SHIELD)) return;

        int delay = 0;
        World w = self.getEntityWorld();
        if (w instanceof ServerWorld sw) {
            delay = Math.max(0, sw.getGameRules().getValue(ModGameRules.SHIELD_RAISE_DELAY_TICKS));
        }

        if (delay <= 0 || self.getItemUseTime() >= delay) {
            cir.setReturnValue(active);
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
        cir.cancel();
    }
}
