package net.hallowed.neatlybetter.content.feature;

import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;


public final class LegacyCombatDamageValues {
    private static final Identifier ATTACK_DAMAGE_ID = Identifier.withDefaultNamespace("base_attack_damage");

    private LegacyCombatDamageValues() {}

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            if (!NTCommonConfig.CONFIG.legacyCombat.get()) {
                return;
            }

            apply(context, Items.WOODEN_SWORD,    3.0);
            apply(context, Items.STONE_SWORD,     4.0);
            apply(context, Items.COPPER_SWORD, 4.0);
            apply(context, Items.GOLDEN_SWORD,    3.0);
            apply(context, Items.IRON_SWORD,      5.0);
            apply(context, Items.DIAMOND_SWORD,   6.0);
            apply(context, Items.NETHERITE_SWORD, 7.0);

            apply(context, Items.WOODEN_SHOVEL,    0.0);
            apply(context, Items.STONE_SHOVEL,     1.0);
            apply(context, Items.COPPER_SHOVEL, 1.0);
            apply(context, Items.GOLDEN_SHOVEL,    0.0);
            apply(context, Items.IRON_SHOVEL,      2.0);
            apply(context, Items.DIAMOND_SHOVEL,   3.0);
            apply(context, Items.NETHERITE_SHOVEL, 4.0);

            apply(context, Items.WOODEN_PICKAXE,    1.0);
            apply(context, Items.STONE_PICKAXE,     2.0);
            apply(context, Items.COPPER_PICKAXE, 2.0);
            apply(context, Items.GOLDEN_PICKAXE,    1.0);
            apply(context, Items.IRON_PICKAXE,      3.0);
            apply(context, Items.DIAMOND_PICKAXE,   4.0);
            apply(context, Items.NETHERITE_PICKAXE, 5.0);

            apply(context, Items.WOODEN_AXE,    2.0);
            apply(context, Items.STONE_AXE,     3.0);
            apply(context, Items.COPPER_AXE, 3.0);
            apply(context, Items.GOLDEN_AXE,    2.0);
            apply(context, Items.IRON_AXE,      4.0);
            apply(context, Items.DIAMOND_AXE,   5.0);
            apply(context, Items.NETHERITE_AXE, 6.0);

            hideAttackSpeedTooltips(context);
        });
    }

    private static void apply(DefaultItemComponentEvents.ModifyContext context, Item item, double attackDamageBonus) {
        context.modify(item, (builder, _, _) -> {
            ItemAttributeModifiers current = builder.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

            ItemAttributeModifiers.Builder rebuilt = ItemAttributeModifiers.builder();
            boolean replaced = false;

            for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
                if (entry.attribute().equals(Attributes.ATTACK_DAMAGE) && entry.modifier().id().equals(ATTACK_DAMAGE_ID)) {
                    rebuilt.add(Attributes.ATTACK_DAMAGE,
                            new AttributeModifier(ATTACK_DAMAGE_ID, attackDamageBonus, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND);
                    replaced = true;
                } else {
                    rebuilt.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
                }
            }

            if (!replaced) {
                rebuilt.add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(ATTACK_DAMAGE_ID, attackDamageBonus, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
            }

            builder.set(DataComponents.ATTRIBUTE_MODIFIERS, rebuilt.build());
        });
    }

    private static void hideAttackSpeedTooltips(DefaultItemComponentEvents.ModifyContext context) {
        context.modify(_ -> true, (builder, _, _) -> {
            if (!builder.contains(DataComponents.ATTRIBUTE_MODIFIERS)) {
                return;
            }

            ItemAttributeModifiers current = builder.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            boolean hasAttackSpeed = current.modifiers().stream()
                    .anyMatch(entry -> entry.attribute().equals(Attributes.ATTACK_SPEED) && !(entry.display() instanceof ItemAttributeModifiers.Display.Hidden));

            if (!hasAttackSpeed) {
                return;
            }

            ItemAttributeModifiers.Builder rebuilt = ItemAttributeModifiers.builder();

            for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
                if (entry.attribute().equals(Attributes.ATTACK_SPEED)) {
                    rebuilt.add(entry.attribute(), entry.modifier(), entry.slot(), ItemAttributeModifiers.Display.hidden());
                } else {
                    rebuilt.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
                }
            }

            builder.set(DataComponents.ATTRIBUTE_MODIFIERS, rebuilt.build());
        });
    }
}