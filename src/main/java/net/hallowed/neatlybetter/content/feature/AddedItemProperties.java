package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.enchantment.Repairable;

import org.jetbrains.annotations.NotNull;

public final class AddedItemProperties {
    private AddedItemProperties() {}

    @SuppressWarnings({"unchecked"})
    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(ctx ->
                ctx.modify(Items.GLISTERING_MELON_SLICE, builder -> {
                    FoodProperties food = new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.6F)
                            .build();
                    builder.set(DataComponents.FOOD, food);
                    builder.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
                })
        );
        DefaultItemComponentEvents.MODIFY.register(ctx ->
                ctx.modify(Items.GLOW_BERRIES, builder -> {
                    Consumable consumable = Consumables.defaultFood()
                            .onConsume(new ApplyStatusEffectsConsumeEffect(
                                    new MobEffectInstance(MobEffects.GLOWING, 200, 0)))
                            .build();
                    builder.set(DataComponents.CONSUMABLE, consumable);
                })
        );
        DefaultItemComponentEvents.MODIFY.register(ctx ->
                ctx.modify(Items.TRIDENT, builder -> {
                    try {
                        Holder<?> shardEntry = Items.PRISMARINE_SHARD.builtInRegistryHolder();
                        HolderSet<?> registryList = HolderSet.direct(shardEntry);
                        Repairable repairable = new Repairable((HolderSet<@NotNull Item>) registryList);
                        builder.set(DataComponents.REPAIRABLE, repairable);
                    } catch (Throwable t) {
                        try {
                            Repairable repairable = new Repairable(HolderSet.direct(Items.PRISMARINE_SHARD.builtInRegistryHolder()));
                            builder.set(DataComponents.REPAIRABLE, repairable);
                        } catch (Throwable ignored) {
                        }
                    }
                })
        );
    }
}
