package net.hallowed.neatlybetter.mixin.entity.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.config.NTCommonConfig;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.component.QuiverContents;
import net.hallowed.neatlybetter.init.ModData;
import net.hallowed.neatlybetter.util.StonecutterMemory;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements StonecutterMemory {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract @NonNull ItemStack getWeaponItem();

    // ===================== (1) Infinity fix & Quiver Arrows =====================

    @Inject(method = "getProjectile", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$getProjectile(ItemStack heldWeapon, CallbackInfoReturnable<ItemStack> cir) {
        if (!(heldWeapon.getItem() instanceof ProjectileWeaponItem)) return;

        Player self = (Player)(Object)this;
        if (self.isCreative()) return;

        var enchLookup = self.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<@NotNull Enchantment> infinity = enchLookup.getOrThrow(Enchantments.INFINITY);

        if (EnchantmentHelper.getItemEnchantmentLevel(infinity, heldWeapon) > 0) {
            if (cir.getReturnValue().isEmpty()) cir.setReturnValue(new ItemStack(Items.ARROW));
            return;
        }

        if (self.level().isClientSide()) return;

        ItemStack quiverStack = neatlybetter$findQuiver(self);
        if (quiverStack == null) return;

        QuiverContents contents = quiverStack.get(ModData.QUIVER_CONTENTS);
        if (contents == null || contents.isEmpty()) return;

        if (!self.isUsingItem()) {
            ItemStackTemplate selected = contents.getSelectedItem();
            ItemStackTemplate toShow = selected != null ? selected : contents.getFirstItem();
            if (toShow != null) cir.setReturnValue(toShow.create());
            return;
        }

        QuiverContents.Mutable mutable = new QuiverContents.Mutable(contents);
        ItemStack arrow = mutable.removeOneFromSelected();
        if (arrow == null || arrow.isEmpty()) return;

        quiverStack.set(ModData.QUIVER_CONTENTS, mutable.toImmutable());
        cir.setReturnValue(arrow);
    }

    @Unique
    private static @Nullable ItemStack neatlybetter$findQuiver(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (neatlybetter$isNonEmptyQuiver(offhand)) return offhand;

        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack s = inventory.getItem(i);
            if (neatlybetter$isNonEmptyQuiver(s)) return s;
        }
        return null;
    }

    @Unique
    private static boolean neatlybetter$isNonEmptyQuiver(ItemStack s) {
        if (s.isEmpty()) return false;
        QuiverContents contents = s.get(ModData.QUIVER_CONTENTS);
        return contents != null && !contents.isEmpty();
    }

    // ===================== (2) Feather not dealing damage =====================

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$featherPush(Entity entity, CallbackInfo ci) {
        if (NTServerConfig.CONFIG.featherNoDamage.isFalse()) return;
        Player self = (Player)(Object)this;
        if (self.getMainHandItem().is(Items.FEATHER)
                && entity instanceof LivingEntity living
                && !self.level().isClientSide()) {

            float base = 0.4F + (self.isSprinting() ? 0.5F : 0.0F);
            float strength = base;
            if (self.level() instanceof net.minecraft.server.level.ServerLevel sw) {
                float modified = EnchantmentHelper.modifyKnockback(
                        sw,
                        self.getMainHandItem(),
                        living,
                        self.damageSources().playerAttack(self),
                        base
                );
                strength = base + (modified - base) * 0.25F;
            }
            strength += (float) self.getAttributeValue(Attributes.ATTACK_KNOCKBACK);

            if (strength > 0.0F) {
                float yaw = self.getYRot();
                double x = net.minecraft.util.Mth.sin(yaw * 0.017453292F);
                double z = -net.minecraft.util.Mth.cos(yaw * 0.017453292F);
                living.knockback(strength, x, z, self.damageSources().playerAttack(self), 0.0F);
                self.setDeltaMovement(self.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                self.setSprinting(false);
            }

            ci.cancel();
        }
    }

    // ===================== (3) Stonecutter Memory =====================

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

    // ===================== (4) No Equip Cooldown =====================

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V")
    )
    private void neatlybetter$skipEquipCooldown(Player player) {
        if (NTCompat.COMBATNOUVEAU || NTCompat.GOLDENAGECOMBAT || NTCommonConfig.CONFIG.legacyCombat.isTrue()) return; {}
    }

    // ===================== (5) Remove Attack Cooldown =====================

    @Inject(
            method = {"getAttackStrengthScale", "getItemSwapScale"},
            at = @At("HEAD"),
            cancellable = true
    )
    private void removeAttackCooldown(float a, CallbackInfoReturnable<Float> cir) {
        if (NTCommonConfig.CONFIG.legacyCombat.isTrue()) {
            cir.setReturnValue(1.0F);
        }
    }

    // ===================== (6) Critical Hits While Sprinting =====================

    @ModifyExpressionValue(
            method = "canCriticalAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSprinting()Z")
    )
    private boolean allowCriticalHitsWhileSprinting(boolean isSprinting, Entity entity) {
        if (NTCommonConfig.CONFIG.legacyCombat.isTrue()) {
            return false;
        }
        return isSprinting;
    }

    // ===================== (7) Sprint Attacks (Don't Stop Sprinting) =====================

    @WrapOperation(
            method = "causeExtraKnockback",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V")
    )
    private void preventSprintStopOnAttack(Player player, boolean sprinting, Operation<Void> original) {
        if (NTCommonConfig.CONFIG.legacyCombat.isTrue()) {
            return;
        }
        original.call(player, sprinting);
    }

    // ===================== (8) Require Sweeping Edge =====================

    @ModifyReturnValue(
            method = "isSweepAttack",
            at = @At(value = "RETURN", ordinal = 0)
    )
    private boolean requireSweepingEdgeForSweep(boolean original) {
        if (NTCommonConfig.CONFIG.legacyCombat.get()) {
            return original && this.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) > 0.0;
        }
        return original;
    }

    // ===================== (9) 0 DMG Attack Knockback =====================

    @ModifyReturnValue(
            method = "hurtServer",
            at = @At("RETURN"),
            slice = @Slice(from = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V"
            ))
    )
    private boolean allowZeroDamageKnockback(boolean hurtServer, ServerLevel level, DamageSource source, float damage) {

        if (!hurtServer && damage == 0.0F && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            return super.hurtServer(level, source, damage);
        }
        return hurtServer;
    }
}