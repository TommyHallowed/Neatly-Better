package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public final class AddedItemProperties {
    private AddedItemProperties() {}

    @SuppressWarnings({"deprecation", "unchecked"})
    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(ctx ->
                ctx.modify(Items.GLISTERING_MELON_SLICE, builder -> {
                    FoodComponent food = new FoodComponent.Builder()
                            .nutrition(4)
                            .saturationModifier(0.6F)
                            .build();
                    builder.add(DataComponentTypes.FOOD, food);
                    builder.add(DataComponentTypes.CONSUMABLE, ConsumableComponents.FOOD);
                })
        );
        DefaultItemComponentEvents.MODIFY.register(ctx ->
                ctx.modify(Items.TRIDENT, builder -> {
                    try {
                        RegistryEntry<?> shardEntry = Items.PRISMARINE_SHARD.getRegistryEntry();
                        RegistryEntryList<?> registryList = RegistryEntryList.of(shardEntry);
                        RepairableComponent repairable = new RepairableComponent((RegistryEntryList<Item>) registryList);
                        builder.add(DataComponentTypes.REPAIRABLE, repairable);
                    } catch (Throwable t) {
                        try {
                            RepairableComponent repairable = new RepairableComponent(RegistryEntryList.of(Items.PRISMARINE_SHARD.getRegistryEntry()));
                            builder.add(DataComponentTypes.REPAIRABLE, repairable);
                        } catch (Throwable ignored) {
                        }
                    }
                })
        );
    }
}
