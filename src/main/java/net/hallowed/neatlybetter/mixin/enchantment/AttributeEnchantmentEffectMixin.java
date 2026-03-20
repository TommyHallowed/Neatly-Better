package net.hallowed.neatlybetter.mixin.enchantment;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentAttributeEffect.class)
public abstract class AttributeEnchantmentEffectMixin {

    @Unique private static final double VANILLA_PER_LEVEL  = 0.15;
    @Unique private static final double DESIRED_PER_LEVEL  = 0.046875;
    @Unique private static final double SCALE = DESIRED_PER_LEVEL / VANILLA_PER_LEVEL;

    @Shadow public abstract Holder<@NotNull Attribute> attribute();

    @Inject(
            method = "getModifier(ILnet/minecraft/util/StringRepresentable;)Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void neatlybetter$weakerProtectionViaGamerule(int level, StringRepresentable suffix,
                                                     CallbackInfoReturnable<AttributeModifier> cir) {

        if (!NTServerConfig.CONFIG.protectionOverhaul.get()) return;
        if (!isBurningTime(attribute())) return;


        AttributeModifier orig = cir.getReturnValue();
        if (orig == null) return;

        // Scale down the modifier’s magnitude
        double scaled = orig.amount() * SCALE;
        cir.setReturnValue(new AttributeModifier(orig.id(), scaled, orig.operation()));
    }

    @Unique
    private static boolean isBurningTime(Holder<@NotNull Attribute> entry) {
        return entry.unwrapKey()
                .map(k -> k.identifier().equals(Identifier.withDefaultNamespace("burning_time")))
                .orElse(false);
    }
}
