package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.enchantment.Repairable;

import java.util.Set;

public final class AddedItemProperties {
    private AddedItemProperties() {}

    private static final Set<Item> NETHERITE_TOOLS = Set.of(
            Items.NETHERITE_SWORD,
            Items.NETHERITE_SHOVEL,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_AXE,
            Items.NETHERITE_HOE
    );

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
                    HolderSet<Item> repairItems = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.PRISMARINE_SHARD));
                    builder.set(DataComponents.REPAIRABLE, new Repairable(repairItems));
                })
        );
        DefaultItemComponentEvents.MODIFY.register(ctx ->
                ctx.modify(NETHERITE_TOOLS, (builder, _) -> {
                    HolderSet<Item> repairItems = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHERITE_SCRAP));
                    builder.set(DataComponents.REPAIRABLE, new Repairable(repairItems));
                })
        );
    }
}