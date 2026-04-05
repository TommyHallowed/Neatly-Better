package net.hallowed.neatlybetter.client.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.hallowed.neatlybetter.client.tooltip.EffectTooltipData;
import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.tooltip.MapPreviewTooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.saveddata.maps.MapId;

import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(Item.class)
public abstract class ItemTooltipsMixin {

    @ModifyReturnValue(
            method = "getTooltipImage(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;",
            at = @At("RETURN")
    )
    private Optional<TooltipComponent> neatlybetter$tooltipImage(
            Optional<TooltipComponent> original,
            ItemStack itemStack
    ) {
        if (original.isPresent()) return original;

        if (itemStack.is(Items.FILLED_MAP)) {
            MapId mapId = itemStack.get(DataComponents.MAP_ID);
            if (mapId != null && NTClientConfig.CONFIG.mapTooltip.get()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    MapItemSavedData mapData = MapItem.getSavedData(mapId, mc.level);
                    if (mapData != null) {
                        return Optional.of(new MapPreviewTooltip(mapId));
                    }
                }
            }
        }

        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null && potionContents.hasEffects() && NTClientConfig.CONFIG.potionEffectIcons.get()) {
            float scale = itemStack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F);
            List<MobEffectInstance> effects = new ArrayList<>();
            potionContents.forEachEffect(effects::add, 1.0F);
            return Optional.of(new EffectTooltipData(
                    effects, scale, neatlybetter$nCopies(effects.size())
            ));
        }

        SuspiciousStewEffects stewEffects = itemStack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
        if (stewEffects != null && !stewEffects.effects().isEmpty() && NTClientConfig.CONFIG.susStewEffectIcons.get() && NTClientConfig.CONFIG.potionEffectIcons.get()) {
            List<MobEffectInstance> effects = stewEffects.effects()
                    .stream()
                    .map(SuspiciousStewEffects.Entry::createEffectInstance)
                    .toList();
            return Optional.of(new EffectTooltipData(
                    effects, 1.0F, neatlybetter$nCopies(effects.size())
            ));
        }

        OminousBottleAmplifier ominous = itemStack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        if (ominous != null && NTClientConfig.CONFIG.potionEffectIcons.get()) {
            List<MobEffectInstance> effects = List.of(
                    new MobEffectInstance(MobEffects.BAD_OMEN, 120000, ominous.value(), false, false, true)
            );
            return Optional.of(new EffectTooltipData(effects, 1.0F, neatlybetter$nCopies(1)));
        }

        Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (consumable != null && NTClientConfig.CONFIG.potionEffectIcons.get()) {
            List<MobEffectInstance> effects = new ArrayList<>();
            List<Float> chances = new ArrayList<>();

            for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
                if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect(
                        List<MobEffectInstance> effects1, float probability
                )) {
                    for (MobEffectInstance effect : effects1) {
                        effects.add(effect);
                        chances.add(probability);
                    }
                }
            }

            if (!effects.isEmpty()) {
                return Optional.of(new EffectTooltipData(effects, 1.0F, chances));
            }
        }

        return original;
    }

    @Unique
    private static List<Float> neatlybetter$nCopies(int n) {
        return Collections.nCopies(n, (float) 1.0);
    }
}
