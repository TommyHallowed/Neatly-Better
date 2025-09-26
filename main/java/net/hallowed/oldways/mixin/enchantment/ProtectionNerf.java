package net.hallowed.oldways.mixin.enchantment;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageUtil.class)
public class ProtectionNerf {
    @Unique private static final ThreadLocal<DamageSource> OLDWAYS$currentSource = new ThreadLocal<>();
    @Unique private static final ThreadLocal<LivingEntity> OLDWAYS$currentEntity = new ThreadLocal<>();

    @Inject(method = "getDamageLeft(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;FF)F",
            at = @At("HEAD"))
    private static void oldways$captureContext(LivingEntity wearer, float amount, DamageSource source,
                                               float armor, float toughness, CallbackInfoReturnable<Float> cir) {
        OLDWAYS$currentSource.set(source);
        OLDWAYS$currentEntity.set(wearer);
    }

    @Inject(method = "getDamageLeft(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;FF)F",
            at = @At("RETURN"))
    private static void oldways$clearContext(LivingEntity wearer, float amount, DamageSource source,
                                             float armor, float toughness, CallbackInfoReturnable<Float> cir) {
        OLDWAYS$currentSource.remove();
        OLDWAYS$currentEntity.remove();
    }

    @Inject(method = "getInflictedDamage(FF)F", at = @At("RETURN"), cancellable = true)
    private static void oldways$capProtections(float damageDealt, float protection,
                                               CallbackInfoReturnable<Float> cir) {
        DamageSource src = OLDWAYS$currentSource.get();
        LivingEntity ent = OLDWAYS$currentEntity.get();
        if (src == null || ent == null) return;

        final int MAX_LEVELS = 16;

        // === Generic Protection ===
        if (!src.isIn(DamageTypeTags.IS_FIRE)
                && !src.isIn(DamageTypeTags.IS_EXPLOSION)
                && !src.isIn(DamageTypeTags.IS_PROJECTILE)) {

            RegistryEntry<Enchantment> e =
                    ent.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
            int levels = EnchantmentHelper.getEquipmentLevel(e, ent);

            float cap = CommonConfigManager.genericProtCap() *
                    MathHelper.clamp((float) levels / MAX_LEVELS, 0f, 1f);

            cir.setReturnValue(damageDealt * (1f - Math.min(protection, cap)));
            return;
        }

        // === Fire Protection ===
        if (src.isIn(DamageTypeTags.IS_FIRE)) {
            RegistryEntry<Enchantment> e =
                    ent.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FIRE_PROTECTION);
            int levels = EnchantmentHelper.getEquipmentLevel(e, ent);

            float cap = CommonConfigManager.fireProtCap() *
                    MathHelper.clamp((float) levels / MAX_LEVELS, 0f, 1f);

            cir.setReturnValue(damageDealt * (1f - Math.min(protection, cap)));
            return;
        }

        // === Blast Protection ===
        if (src.isIn(DamageTypeTags.IS_EXPLOSION)) {
            RegistryEntry<Enchantment> e =
                    ent.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.BLAST_PROTECTION);
            int levels = EnchantmentHelper.getEquipmentLevel(e, ent);

            float cap = CommonConfigManager.blastProtCap() *
                    MathHelper.clamp((float) levels / MAX_LEVELS, 0f, 1f);

            cir.setReturnValue(damageDealt * (1f - Math.min(protection, cap)));
            return;
        }

        // === Projectile Protection ===
        if (src.isIn(DamageTypeTags.IS_PROJECTILE)) {
            RegistryEntry<Enchantment> e =
                    ent.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.PROJECTILE_PROTECTION);
            int levels = EnchantmentHelper.getEquipmentLevel(e, ent);

            float cap = CommonConfigManager.projectileProtCap() *
                    MathHelper.clamp((float) levels / MAX_LEVELS, 0f, 1f);

            cir.setReturnValue(damageDealt * (1f - Math.min(protection, cap)));
        }
    }
}
