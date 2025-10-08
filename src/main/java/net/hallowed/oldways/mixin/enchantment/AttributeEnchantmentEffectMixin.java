package net.hallowed.oldways.mixin.enchantment;

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

@Mixin(AttributeEnchantmentEffect.class)
public abstract class AttributeEnchantmentEffectMixin {

    @Unique private static final double VANILLA_PER_LEVEL  = 0.15;
    @Unique private static final double DESIRED_PER_LEVEL  = 0.046875;
    @Unique private static final double SCALE = DESIRED_PER_LEVEL / VANILLA_PER_LEVEL;

    @Shadow public abstract RegistryEntry<EntityAttribute> attribute();

    @Inject(
            method = "createAttributeModifier(ILnet/minecraft/util/StringIdentifiable;)Lnet/minecraft/entity/attribute/EntityAttributeModifier;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void oldways$weakerProtectionViaGamerule(int level, StringIdentifiable suffix,
                                                     CallbackInfoReturnable<EntityAttributeModifier> cir) {
        if (!isBurningTime(attribute())) return;


        EntityAttributeModifier orig = cir.getReturnValue();
        if (orig == null) return;

        // Scale down the modifier’s magnitude
        double scaled = orig.value() * SCALE;
        cir.setReturnValue(new EntityAttributeModifier(orig.id(), scaled, orig.operation()));
    }

    @Unique
    private static boolean isBurningTime(RegistryEntry<EntityAttribute> entry) {
        return entry.getKey()
                .map(k -> k.getValue().equals(Identifier.ofVanilla("burning_time")))
                .orElse(false);
    }
}
