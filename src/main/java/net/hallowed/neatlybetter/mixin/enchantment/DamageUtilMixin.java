package net.hallowed.neatlybetter.mixin.enchantment;

import net.hallowed.neatlybetter.util.ProtectionContext;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatRules.class)
public abstract class DamageUtilMixin {

    /* ===================== hard-coded caps =====================
     * Caps are FRACTIONS of vanilla's maximum enchantment reduction (which is 80% at full levels).
     * Example: to cap to 50% total at full Prot IV set, use 0.625f (because 0.625 * 0.8 = 0.5).
     * They scale linearly with total levels out of 16 (4 armor pieces × level 4).
     */
    @Unique
    private static final float GENERIC_CAP_FRACTION     = 0.4375f; // affects most damage
    @Unique
    private static final float FIRE_CAP_FRACTION        = 0.625f; // only when IS_FIRE
    @Unique
    private static final float BLAST_CAP_FRACTION       = 0.625f; // only when IS_EXPLOSION
    @Unique
    private static final float PROJECTILE_CAP_FRACTION  = 0.75f; // only when IS_PROJECTILE

    @Unique
    private static final float MAX_LEVELS  = 16.0f; // 4 pieces × level 4
    @Unique
    private static final float MAX_POINTS  = 20.0f; // vanilla protection points hard-cap

    @Inject(method = "getDamageAfterMagicAbsorb(FF)F", at = @At("RETURN"), cancellable = true)
    private static void neatlybetter$capProtectionPoints(float damageDealt, float protectionPoints,
                                                    CallbackInfoReturnable<Float> cir) {

        final DamageSource src = ProtectionContext.src();
        final LivingEntity ent = ProtectionContext.ent();
        if (src == null || ent == null) return;

        // If vanilla says there are no protection points, nothing to clamp.
        if (protectionPoints <= 0.0f) return;

        final boolean isFire       = src.is(DamageTypeTags.IS_FIRE);
        final boolean isExplosion  = src.is(DamageTypeTags.IS_EXPLOSION);
        final boolean isProjectile = src.is(DamageTypeTags.IS_PROJECTILE);

        // Sum enchantment levels across equipped armor (max practical: 16)
        int lvlProt       = getTotalLevel(ent, Enchantments.PROTECTION);
        int lvlFireProt   = getTotalLevel(ent, Enchantments.FIRE_PROTECTION);
        int lvlBlastProt  = getTotalLevel(ent, Enchantments.BLAST_PROTECTION);
        int lvlProjProt   = getTotalLevel(ent, Enchantments.PROJECTILE_PROTECTION);

        // Convert FRACTIONS to protection POINT caps (0..20), scaled by levels/16
        float allowed = 0.0f;
        allowed += clamp01(GENERIC_CAP_FRACTION) * (lvlProt      / MAX_LEVELS) * MAX_POINTS;
        if (isFire)       allowed += clamp01(FIRE_CAP_FRACTION)       * (lvlFireProt  / MAX_LEVELS) * MAX_POINTS;
        if (isExplosion)  allowed += clamp01(BLAST_CAP_FRACTION)      * (lvlBlastProt / MAX_LEVELS) * MAX_POINTS;
        if (isProjectile) allowed += clamp01(PROJECTILE_CAP_FRACTION) * (lvlProjProt  / MAX_LEVELS) * MAX_POINTS;

        // Respect vanilla’s hard 20-point cap
        allowed = Mth.clamp(allowed, 0.0F, MAX_POINTS);

        // Clamp incoming protection to our allowed cap, then apply vanilla formula
        float p = Math.min(Mth.clamp(protectionPoints, 0.0F, MAX_POINTS), allowed);
        float result = damageDealt * (1.0F - p / 25.0F);
        cir.setReturnValue(result);
    }

    @Unique
    private static int getTotalLevel(LivingEntity ent, ResourceKey<@NotNull Enchantment> key) {
        Holder<@NotNull Enchantment> entry = ent.registryAccess().getOrThrow(key);
        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = ent.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                total += EnchantmentHelper.getItemEnchantmentLevel(entry, stack);
            }
        }
        return total;
    }

    @Unique
    private static float clamp01(float v) {
        if (Float.isNaN(v)) return 0f;
        return v < 0f ? 0f : (Math.min(v, 1f));
    }
}
