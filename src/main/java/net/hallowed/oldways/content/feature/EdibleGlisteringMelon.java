package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Items;

public final class EdibleGlisteringMelon {
    private EdibleGlisteringMelon() {}

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
    }
}
