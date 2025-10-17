package net.hallowed.oldways.mixin.entity.ai;

import net.hallowed.oldways.api.OWCompat;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.item.equipment.trim.ArmorTrimPatterns;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

    @Inject(
            method = "isWearingPiglinSafeArmor(Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void oldways$goldTrimPacifies(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (OWCompat.RESPECTMYTRIMS) return;
        if (cir.getReturnValue()) return;

        for (EquipmentSlot slot : AttributeModifierSlot.ARMOR) {
            ArmorTrim trimComp = entity.getEquippedStack(slot)
                    .getComponents().get(DataComponentTypes.TRIM);
            if (trimComp == null) continue;

            RegistryEntry<ArmorTrimMaterial> material = trimComp.material();
            RegistryEntry<ArmorTrimPattern>  pattern  = trimComp.pattern();

            boolean isGoldTrim = material != null && material.matchesKey(ArmorTrimMaterials.GOLD);
            boolean isSnout    = pattern  != null && pattern.matchesKey(ArmorTrimPatterns.SNOUT);

            if (isGoldTrim || isSnout) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
