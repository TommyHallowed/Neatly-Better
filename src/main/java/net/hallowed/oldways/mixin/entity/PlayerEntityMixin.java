package net.hallowed.oldways.mixin.entity;

import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.enchant.OldEnchantCostContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Combined player mixin:
 * 1) Infinity fix (virtual arrow) — guarded by CommonConfigManager.infinityFixEnabled()
 * 2) OldEnchant extra level cost — guarded by CommonConfigManager.oldEnchant()
 * Behavior is unchanged from your original two mixins.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    /* ------------------ (1) Infinity fix: virtual arrow when bow has Infinity ------------------ */
    @Inject(method = "getProjectileType", at = @At("RETURN"), cancellable = true)
    private void oldways$virtualArrowForInfinity(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        if (!CommonConfigManager.infinityFixEnabled()) return;
        // vanilla already found a projectile?
        if (!cir.getReturnValue().isEmpty()) return;

        if (!(weapon.getItem() instanceof BowItem)) return;

        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.getAbilities().creativeMode) return;

        var enchLookup = self.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> infinity = enchLookup.getOrThrow(Enchantments.INFINITY);

        if (EnchantmentHelper.getLevel(infinity, weapon) <= 0) return;

        // Pretend we have a normal arrow so shooting is allowed
        cir.setReturnValue(new ItemStack(Items.ARROW));
    }

    /* ------------------ (2) Old Enchant: pay the full displayed level cost ------------------ */
    @Inject(method = "applyEnchantmentCosts(Lnet/minecraft/item/ItemStack;I)V", at = @At("HEAD"))
    private void oldways$topUp(ItemStack stack, int vanillaLevels, CallbackInfo ci) {
        if (!CommonConfigManager.oldEnchant()) return;
        Integer full = OldEnchantCostContext.peekRequired();
        if (full == null) return;
        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.getAbilities().creativeMode) return;
        int extra = full - Math.max(1, vanillaLevels);
        if (extra > 0) self.addExperienceLevels(-extra);
    }

    /* ------------------ (3) Feather not dealing damage ------------------ */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void oldways$featherPush(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.getMainHandStack().isOf(Items.FEATHER)
                && target instanceof LivingEntity living
                && !self.getWorld().isClient) {

            float base = 0.4F + (self.isSprinting() ? 0.5F : 0.0F);
            float strength = base;
            if (self.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                float modified = net.minecraft.enchantment.EnchantmentHelper.modifyKnockback(
                        sw,
                        self.getMainHandStack(),
                        living,
                        self.getDamageSources().playerAttack(self),
                        base
                );
                strength = base + (modified - base) * 0.25F;
            }
            strength += (float) self.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ATTACK_KNOCKBACK);

            if (strength > 0.0F) {
                float yaw = self.getYaw();
                double x = net.minecraft.util.math.MathHelper.sin(yaw * 0.017453292F);
                double z = -net.minecraft.util.math.MathHelper.cos(yaw * 0.017453292F);
                living.takeKnockback(strength, x, z);
                self.setVelocity(self.getVelocity().multiply(0.6D, 1.0D, 0.6D));
                self.setSprinting(false);
            }

            ci.cancel();
        }
    }
}
