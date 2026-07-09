package net.hallowed.neatlybetter.mixin.entity.ai;

import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.WeakHashMap;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {

    @Inject(
            method = "isWearingSafeArmor(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void neatlybetter$goldTrimPacifies(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!NTServerConfig.CONFIG.piglinRespectsTrims.get()) return;
        if (NTCompat.RESPECTMYTRIMS) return;
        if (cir.getReturnValue()) return;

        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            ArmorTrim trimComp = livingEntity.getItemBySlot(slot)
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

    @Unique
    private static final WeakHashMap<Piglin, Long> neatlybetter$lastSoundTick = new WeakHashMap<>();

    @Redirect(
            method = "updateActivity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;getSoundForCurrentActivity(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Ljava/util/Optional;")
    )
    private static Optional<SoundEvent> neatlybetter$throttleActivitySound(Piglin body) {
        long tick = body.level().getGameTime();
        Long last = neatlybetter$lastSoundTick.get(body);
        if (last != null && tick - last < 40) {
            return Optional.empty();
        }
        neatlybetter$lastSoundTick.put(body, tick);
        return PiglinAi.getSoundForCurrentActivity(body);
    }
}
