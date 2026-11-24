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

    /**
     * Netherite gear covered by the {@code anvil_netherite_repair} server config: also accepts netherite scrap
     * as a repair item (25% per scrap, see ItemStackMixin) and is fully repaired by a single netherite ingot
     * in an anvil (see AnvilMenuMixin). Checked at runtime, so the item components stay vanilla.
     */
    public static final Set<Item> NETHERITE_REPAIR_ITEMS = Set.of(
            Items.NETHERITE_SWORD,
            Items.NETHERITE_SHOVEL,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_AXE,
            Items.NETHERITE_HOE,
            Items.NETHERITE_SPEAR,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS
    );

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(ctx -> {
            ctx.modify(Items.GLISTERING_MELON_SLICE, builder -> {
                FoodProperties food = new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(0.6F)
                        .build();
                builder.set(DataComponents.FOOD, food);
                builder.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            });
            ctx.modify(Items.GLOW_BERRIES, builder -> {
                Consumable consumable = Consumables.defaultFood()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(
                                new MobEffectInstance(MobEffects.GLOWING, 200, 0)))
                        .build();
                builder.set(DataComponents.CONSUMABLE, consumable);
            });
            ctx.modify(Items.TRIDENT, builder -> {
                HolderSet<Item> repairItems = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.PRISMARINE_SHARD));
                builder.set(DataComponents.REPAIRABLE, new Repairable(repairItems));
            });
        });
    }
}