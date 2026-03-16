package net.hallowed.neatlybetter.mixin.entity.ai;

import net.hallowed.neatlybetter.api.NTCompat;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinBrainMixin {

    @Inject(
            method = "isWearingSafeArmor(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void neatlybetter$goldTrimPacifies(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (NTCompat.RESPECTMYTRIMS) return;
        if (cir.getReturnValue()) return;

        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            ArmorTrim trimComp = entity.getItemBySlot(slot)
                    .getComponents().get(DataComponents.TRIM);
            if (trimComp == null) continue;

            Holder<@NotNull TrimMaterial> material = trimComp.material();
            Holder<@NotNull TrimPattern>  pattern  = trimComp.pattern();

            boolean isGoldTrim = material.is(TrimMaterials.GOLD);
            boolean isSnout    = pattern.is(TrimPatterns.SNOUT);

            if (isGoldTrim || isSnout) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
