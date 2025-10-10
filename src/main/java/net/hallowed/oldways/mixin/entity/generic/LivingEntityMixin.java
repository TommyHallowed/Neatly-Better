package net.hallowed.oldways.mixin.entity.generic;
import net.hallowed.oldways.init.ModGameRules;
import net.hallowed.oldways.util.ProtectionContext;

import net.hallowed.oldways.util.EntityInsideFireHandler;
import net.hallowed.oldways.util.FireSourceHolder;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
        if (cir.getReturnValue() && (Object)this instanceof PlayerEntity player) {
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
        World w = self.getWorld();
        if (w instanceof ServerWorld sw) {
            delay = Math.max(0, sw.getGameRules().getInt(ModGameRules.SHIELD_RAISE_DELAY_TICKS));
        }

        if (delay <= 0 || self.getItemUseTime() >= delay) {
            cir.setReturnValue(active);
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
        cir.cancel();
    }

    /* ===================== 6) jeb_ sheep rainbow wool drops replacement ===================== */

    @Inject(
            method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At("TAIL")
    )
    private void oldways$replaceWoolWithRainbow(ServerWorld world, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof SheepEntity sheep)) return;

        if (!sheep.hasCustomName()) return;
        String name = sheep.getCustomName() == null ? "" : sheep.getCustomName().getString();
        if (!"jeb_".equals(name)) return;

        Box area = sheep.getBoundingBox().expand(2.0);
        var nearby = world.getEntitiesByClass(ItemEntity.class, area, ie ->
                !ie.isRemoved()
                        && ie.getOwner() == null
                        && ie.getStack().isIn(ItemTags.WOOL)
                        && ie.age <= 5
        );

        int totalWool = 0;
        for (ItemEntity ie : nearby) {
            totalWool += ie.getStack().getCount();
            ie.discard();
        }
        if (totalWool <= 0) return;

        var rainbow = Registries.ITEM.get(Identifier.of("old-ways", "rainbow_wool"));
        if (rainbow == null) return;

        sheep.dropStack(world, new ItemStack(rainbow, totalWool));
    }

    /* ===================== 7) Hostile mobs XP boost ===================== */
    @Unique private static final float HOSTILE_XP_MULTIPLIER = 2.0f;
    @Unique private static final int HOSTILE_BONUS_XP_CAP = 200;

    @Unique
    private static boolean oldways$shouldBoost(LivingEntity self) {
        EntityType<?> t = self.getType();
        if (t == EntityType.ENDER_DRAGON) return false;
        if (!(self instanceof HostileEntity)) return false;
        return self.getType().getSpawnGroup() == SpawnGroup.MONSTER;
    }

    @Unique
    private static int oldways$boostWithCap(int base) {
        if (HOSTILE_XP_MULTIPLIER <= 1.0f) return base;

        int bonus = Math.round(base * (HOSTILE_XP_MULTIPLIER - 1.0f));
        if (bonus < 0) bonus = 0;
        if (bonus > HOSTILE_BONUS_XP_CAP) bonus = HOSTILE_BONUS_XP_CAP;
        return base + bonus;
    }

    @Inject(
            method = "getExperienceToDrop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void oldways$boostHostileXp(ServerWorld world, Entity attacker, CallbackInfoReturnable<Integer> cir) {
        int base = cir.getReturnValue();
        if (base <= 0) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (oldways$shouldBoost(self)) {
            cir.setReturnValue(oldways$boostWithCap(base));
        }
    }

    @Inject(
            method = "getExperienceToDrop(Lnet/minecraft/server/world/ServerWorld;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void oldways$boostHostileXpNoAttacker(ServerWorld world, CallbackInfoReturnable<Integer> cir) {
        int base = cir.getReturnValue();
        if (base <= 0) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (oldways$shouldBoost(self)) {
            cir.setReturnValue(oldways$boostWithCap(base));
        }
    }

    /* ===================== 8) Deal double damage if on soul fire ===================== */

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"), cancellable = true)
    private void oldways$boostOnFireFromSoulFire(DamageSource source, float amount,
                                                 CallbackInfoReturnable<Float> cir) {
        if (!source.isOf(DamageTypes.ON_FIRE)) return;

        final Entity self = (Entity)(Object)this;
        if (!(self instanceof FireSourceHolder holder)) return;

        if (holder.oldways$getLastFireSource() != Blocks.SOUL_FIRE) return;
        if (EntityInsideFireHandler.isInsideSoulFire(self)) return;

        float out = cir.getReturnValueF();
        if (out > 0.0F) {
            cir.setReturnValue(out * 2.0F);
        }
    }
}
