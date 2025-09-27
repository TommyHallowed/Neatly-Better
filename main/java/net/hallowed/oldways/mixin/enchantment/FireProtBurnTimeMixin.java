package net.hallowed.oldways.mixin.enchantment;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales Fire Protection's 'minecraft:burning_time' attribute effect.
 * Uses hard-coded per-level target value, and only applies when
 * CommonConfigManager.protectionNerfEnabled() is true.
 * Vanilla is 0.15 (15%) per level. Set DESIRED_PER_LEVEL to your target.
 */
@Mixin(AttributeEnchantmentEffect.class)
public abstract class FireProtBurnTimeMixin {

    // ======= Hard-coded tuning knobs =======
    /** Vanilla per-level reduction is 0.15 (15%). */
    @Unique
    private static final double VANILLA_PER_LEVEL  = 0.15;
    /** Your desired per-level reduction (e.g., 0.10 = 10%/level). */
    @Unique
    private static final double DESIRED_PER_LEVEL = 0.046875;

    @Shadow public abstract RegistryEntry<EntityAttribute> attribute();

    @Inject(
            method = "createAttributeModifier(ILnet/minecraft/util/StringIdentifiable;)Lnet/minecraft/entity/attribute/EntityAttributeModifier;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void oldways$scaleBurnTimeModifier(int level, StringIdentifiable suffix,
                                               CallbackInfoReturnable<EntityAttributeModifier> cir) {
        // Global toggle: do nothing if disabled
        if (!CommonConfigManager.protectionNerfEnabled()) return;

        // Only adjust the fire-prot 'burning_time' attribute
        if (!isBurningTime(attribute())) return;

        EntityAttributeModifier orig = cir.getReturnValue();
        if (orig == null) return;

        // Scale relative to vanilla (keep sign & operation)
        double scale = (VANILLA_PER_LEVEL > 0.0) ? (DESIRED_PER_LEVEL / VANILLA_PER_LEVEL) : 1.0;
        if (scale == 1.0) return; // no change needed

        double scaledValue = orig.value() * scale;
        cir.setReturnValue(new EntityAttributeModifier(orig.id(), scaledValue, orig.operation()));
    }

    @Unique
    private static boolean isBurningTime(RegistryEntry<EntityAttribute> entry) {
        return entry.getKey()
                .map(k -> k.getValue().equals(Identifier.ofVanilla("burning_time")))
                .orElse(false);
    }
}
