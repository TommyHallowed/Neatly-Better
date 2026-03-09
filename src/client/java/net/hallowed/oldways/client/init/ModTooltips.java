package net.hallowed.oldways.client.init;


import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import net.hallowed.oldways.content.item.MapBuilderItem;
import net.hallowed.oldways.init.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.List;

public final class ModTooltips {
    private ModTooltips() {}

    private static final MutableComponent GLOW_SAC_HINT =
            Component.literal("Can be used on trimmed items").withStyle(ChatFormatting.GRAY);
    private static final MutableComponent ECHO_SHARD_HINT =
            Component.literal("Can be used on trimmed armor").withStyle(ChatFormatting.GRAY);

    private static final MutableComponent EMISSIVE_GLOW_LINE =
            Component.literal(" Glowing").withStyle(ChatFormatting.AQUA);
    private static final MutableComponent PULSING_ECHO_LINE =
            Component.literal(" Pulsing").withStyle(ChatFormatting.DARK_AQUA);

    public static void init() {
        ItemTooltipCallback.EVENT.register(ModTooltips::onTooltip);
    }

    private static void onTooltip(ItemStack stack,
                                  Item.TooltipContext ctx,
                                  TooltipFlag type,
                                  List<Component> lines) {
        if (stack == null || stack.isEmpty()) return;

        if (stack.is(Items.GLOW_INK_SAC)) {
            addBasicUnderName(lines, GLOW_SAC_HINT);
        }
        if (stack.is(Items.ECHO_SHARD)) {
            addBasicUnderName(lines, ECHO_SHARD_HINT);
        }
        if (stack.is(Items.TOTEM_OF_UNDYING)) {
            addBasicUnderName(lines, Component.literal("Cooldown on use: 60s").withStyle(ChatFormatting.GRAY));
        }
        if (stack.getItem() instanceof MapBuilderItem) {
            lines.add(Component.empty());
            lines.add(Component.literal(" Right-Click: Set Top-Left Corner").withStyle(ChatFormatting.YELLOW));
            lines.add(Component.literal(" Left-Click: Set Bottom-Right Corner").withStyle(ChatFormatting.YELLOW));
            lines.add(Component.literal(" Right-Click in area: Build Map").withStyle(ChatFormatting.YELLOW));
            lines.add(Component.literal(" Shift + Z: Change Map Zoom").withStyle(ChatFormatting.YELLOW));
            lines.add(Component.empty());
            lines.add(Component.literal("Requires Empty Maps & Item Frames").withStyle(ChatFormatting.RED));
        }

        // --- Equip Tooltip ---
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            EquipmentSlot slot = equippable.slot();
            // Filter to only affect Armor and Elytra (ignores offhand/saddles)
            if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST ||
                    slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {

                int insertPos = lines.size();

                // If Advanced Tooltips (F3+H) are enabled, find the start of the advanced section
                if (type.isAdvanced()) {
                    String regName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    for (int i = 0; i < lines.size(); i++) {
                        // The item's registry name (e.g., "minecraft:iron_chestplate") is always shown
                        if (lines.get(i).getString().contains(regName)) {
                            insertPos = i;
                            // If the item is damaged, durability is displayed exactly one line above the registry name
                            if (i > 0 && stack.isDamaged() && lines.get(i - 1).getString().contains("/")) {
                                insertPos = i - 1;
                            }
                            break;
                        }
                    }
                }
                lines.add(insertPos, Component.empty());
                lines.add(insertPos + 1, Component.literal("Right Click To Equip").withStyle(ChatFormatting.YELLOW));
            }
        }

        // Updated method call to handle all special trims
        appendSpecialTrimLines(stack, lines);
    }

    public static void addBasicUnderName(List<Component> lines, Component tip) {
        final int insertAt = Math.min(lines.size(), 1);
        if (insertAt < lines.size()) {
            Component existing = lines.get(insertAt);
            if (existing.getString().equals(tip.getString())) return;
            if (existing.getString().isBlank()) {
                lines.set(insertAt, tip);
                return;
            }
        }
        lines.add(insertAt, tip);
    }

    private static void appendSpecialTrimLines(ItemStack stack, List<Component> lines) {
        boolean emissive = stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false);
        boolean pulsing = stack.getOrDefault(ModDataComponents.PULSING_TRIM, false);

        // If the item has neither effect, do nothing
        if (!emissive && !pulsing) return;

        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null) return;

        // Choose the correct text line to add
        Component lineToAdd = emissive ? EMISSIVE_GLOW_LINE : PULSING_ECHO_LINE;

        int up = findUpgradeHeaderIndex(lines);
        if (up >= 0) {
            int idx = up + 1;
            int nonEmpty = 0;
            for (; idx < lines.size() && nonEmpty < 2; idx++) {
                if (!lines.get(idx).getString().isBlank()) nonEmpty++;
            }
            lines.add(idx, lineToAdd);
            return;
        }

        lines.add(Math.min(2, lines.size()), lineToAdd);
    }

    private static int findUpgradeHeaderIndex(List<Component> lines) {
        for (int i = 0, n = lines.size(); i < n; i++) {
            String s = lines.get(i).getString();
            s = s.trim();
            if (s.equalsIgnoreCase("Upgrade:") || s.equalsIgnoreCase("Upgrade")) {
                return i;
            }
        }
        return -1;
    }
}