package net.hallowed.neatlybetter.mixin.entity.player;

import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.util.StonecutterMemory;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements StonecutterMemory {

    /* ------------------ (1) Infinity fix: virtual arrow when bow has Infinity ------------------ */
    @Inject(method = "getProjectile", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$virtualArrowForInfinity(ItemStack heldWeapon, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) return;

        if (!(heldWeapon.getItem() instanceof ProjectileWeaponItem)) return;

        Player self = (Player)(Object)this;
        if (self.getAbilities().instabuild) return;

        var enchLookup = self.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<@NotNull Enchantment> infinity = enchLookup.getOrThrow(Enchantments.INFINITY);

        if (EnchantmentHelper.getItemEnchantmentLevel(infinity, heldWeapon) <= 0) return;

        cir.setReturnValue(new ItemStack(Items.ARROW));
    }

    /* ------------------ (2) Feather not dealing damage ------------------ */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$featherPush(Entity entity, CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.featherNoDamage.get()) return;
        Player self = (Player)(Object)this;
        if (self.getMainHandItem().is(Items.FEATHER)
                && entity instanceof LivingEntity living
                && !self.level().isClientSide()) {

            float base = 0.4F + (self.isSprinting() ? 0.5F : 0.0F);
            float strength = base;
            if (self.level() instanceof net.minecraft.server.level.ServerLevel sw) {
                float modified = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyKnockback(
                        sw,
                        self.getMainHandItem(),
                        living,
                        self.damageSources().playerAttack(self),
                        base
                );
                strength = base + (modified - base) * 0.25F;
            }
            strength += (float) self.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK);

            if (strength > 0.0F) {
                float yaw = self.getYRot();
                double x = net.minecraft.util.Mth.sin(yaw * 0.017453292F);
                double z = -net.minecraft.util.Mth.cos(yaw * 0.017453292F);
                living.knockback(strength, x, z);
                self.setDeltaMovement(self.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                self.setSprinting(false);
            }

            ci.cancel();
        }
    }

    /* ------------------ (3) Stonecutter Memory ------------------ */
    @Unique
    private String neatlybetter$lastCraftedStonecutterItem = "";

    @Override
    public void neatlybetter$setLastCraftedItem(String id) {
        this.neatlybetter$lastCraftedStonecutterItem = id == null ? "" : id;
    }

    @Override
    public String neatlybetter$getLastCraftedItem() {
        return this.neatlybetter$lastCraftedStonecutterItem;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void neatlybetter$writeStonecutterMemory(ValueOutput output, CallbackInfo ci) {
        if (!this.neatlybetter$lastCraftedStonecutterItem.isEmpty()) {
            output.putString("neatlybetter_last_stonecutter_item", this.neatlybetter$lastCraftedStonecutterItem);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void neatlybetter$readStonecutterMemory(ValueInput input, CallbackInfo ci) {
        this.neatlybetter$lastCraftedStonecutterItem = input.getStringOr("neatlybetter_last_stonecutter_item", "");
    }

    /* ------------------ (4) No Equip Cooldown ------------------ */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"
            )
    )
    private void neatlybetter$skipEquipCooldown(Player player) {
        if (NTCompat.COMBATNOUVEAU || NTCompat.GOLDENAGECOMBAT) return; {}
    }
}
