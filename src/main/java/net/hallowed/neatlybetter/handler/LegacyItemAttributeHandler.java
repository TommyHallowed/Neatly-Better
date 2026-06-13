package net.hallowed.neatlybetter.handler;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalDouble;

public final class LegacyItemAttributeHandler {

    private LegacyItemAttributeHandler() {}

    public static @Nullable ItemAttributeModifiers applyLegacyAttackDamage(
            Item item, ItemAttributeModifiers current) {

        OptionalDouble typeBonus = getLegacyTypeBonus(item);
        if (typeBonus.isEmpty()) return null;

        double materialBonus = getExistingAttackDamage(current).orElse(0.0);

        double newValue = materialBonus + typeBonus.getAsDouble();

        return setAttackDamageAttribute(current, newValue);
    }

    private static OptionalDouble getLegacyTypeBonus(Item item) {
        Tool tool = item.components().get(DataComponents.TOOL);
        if (tool == null) return OptionalDouble.empty();

        if (toolHasRuleForTag(tool, BlockTags.MINEABLE_WITH_AXE))    return OptionalDouble.of(-4.0);
        if (toolHasRuleForTag(tool, BlockTags.MINEABLE_WITH_SHOVEL)) return OptionalDouble.of(-1.5);

        return OptionalDouble.empty();
    }

    private static boolean toolHasRuleForTag(Tool tool, TagKey<Block> tag) {
        Identifier tagId = tag.location();
        return tool.rules().stream().anyMatch(rule ->
                rule.blocks().unwrapKey()
                        .map(key -> key.location().equals(tagId))
                        .orElse(false)
        );
    }

    private static OptionalDouble getExistingAttackDamage(ItemAttributeModifiers modifiers) {
        return modifiers.modifiers().stream()
                .filter(e -> e.attribute().is(Attributes.ATTACK_DAMAGE)
                        && e.slot() == EquipmentSlotGroup.MAINHAND
                        && e.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
                .mapToDouble(e -> e.modifier().amount())
                .findFirst();
    }

    private static ItemAttributeModifiers setAttackDamageAttribute(
            ItemAttributeModifiers current, double newValue) {

        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(current.modifiers());

        AttributeModifier newMod = new AttributeModifier(
                Item.BASE_ATTACK_DAMAGE_ID,
                newValue,
                AttributeModifier.Operation.ADD_VALUE);

        ItemAttributeModifiers.Entry newEntry = new ItemAttributeModifiers.Entry(
                Attributes.ATTACK_DAMAGE,
                newMod,
                EquipmentSlotGroup.MAINHAND,
                current.modifiers().stream()
                        .filter(e -> e.attribute().is(Attributes.ATTACK_DAMAGE)
                                && e.slot() == EquipmentSlotGroup.MAINHAND)
                        .findFirst()
                        .map(ItemAttributeModifiers.Entry::display)
                        .orElse(ItemAttributeModifiers.Display.hidden()));

        ListIterator<ItemAttributeModifiers.Entry> it = entries.listIterator();
        boolean replaced = false;
        while (it.hasNext()) {
            ItemAttributeModifiers.Entry e = it.next();
            if (e.attribute().is(Attributes.ATTACK_DAMAGE)
                    && e.slot() == EquipmentSlotGroup.MAINHAND) {
                it.set(newEntry);
                replaced = true;
                break;
            }
        }
        if (!replaced) entries.add(newEntry);

        return new ItemAttributeModifiers(ImmutableList.copyOf(entries));
    }
}