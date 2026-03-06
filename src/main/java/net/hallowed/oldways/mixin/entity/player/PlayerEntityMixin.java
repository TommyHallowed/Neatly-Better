package net.hallowed.oldways.mixin.entity.player;

import net.hallowed.oldways.util.StonecutterMemory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements StonecutterMemory {

    /* ------------------ (1) Infinity fix: virtual arrow when bow has Infinity ------------------ */
    @Inject(method = "getProjectileType", at = @At("RETURN"), cancellable = true)
    private void oldways$virtualArrowForInfinity(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) return;

        if (!(weapon.getItem() instanceof RangedWeaponItem)) return;

        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.getAbilities().creativeMode) return;

        var enchLookup = self.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> infinity = enchLookup.getOrThrow(Enchantments.INFINITY);

        if (EnchantmentHelper.getLevel(infinity, weapon) <= 0) return;

        cir.setReturnValue(new ItemStack(Items.ARROW));
    }

    /* ------------------ (2) Feather not dealing damage ------------------ */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void oldways$featherPush(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.getMainHandStack().isOf(Items.FEATHER)
                && target instanceof LivingEntity living
                && !self.getEntityWorld().isClient()) {

            float base = 0.4F + (self.isSprinting() ? 0.5F : 0.0F);
            float strength = base;
            if (self.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
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

    /* ------------------ (3) Stonecutter Memory ------------------ */

    @Unique
    private String oldways$lastCraftedStonecutterItem = "";

    @Override
    public void oldways$setLastCraftedItem(String id) {
        this.oldways$lastCraftedStonecutterItem = id == null ? "" : id;
    }

    @Override
    public String oldways$getLastCraftedItem() {
        return this.oldways$lastCraftedStonecutterItem;
    }

    // FIX: Using the new modern WriteView system!
    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void oldways$writeStonecutterMemory(WriteView view, CallbackInfo ci) {
        if (!this.oldways$lastCraftedStonecutterItem.isEmpty()) {
            view.putString("oldways_last_stonecutter_item", this.oldways$lastCraftedStonecutterItem);
        }
    }

    // FIX: Using the new modern ReadView system!
    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void oldways$readStonecutterMemory(ReadView view, CallbackInfo ci) {
        this.oldways$lastCraftedStonecutterItem = view.getString("oldways_last_stonecutter_item", "");
    }
}
