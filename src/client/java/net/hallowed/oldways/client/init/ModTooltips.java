package net.hallowed.oldways.client.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.hallowed.oldways.content.item.MapBuilderItem;
import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class ModTooltips {
    private ModTooltips() {}

    private static final MutableText GLOW_SAC_HINT =
            Text.literal("Can be used on trimmed items").formatted(Formatting.GRAY);
    private static final MutableText ECHO_SHARD_HINT =
            Text.literal("Can be used on trimmed armor").formatted(Formatting.GRAY);

    private static final MutableText EMISSIVE_GLOW_LINE =
            Text.literal(" Glowing").formatted(Formatting.AQUA);
    private static final MutableText PULSING_ECHO_LINE =
            Text.literal(" Pulsing").formatted(Formatting.DARK_AQUA);

    public static void init() {
        ItemTooltipCallback.EVENT.register(ModTooltips::onTooltip);
    }

    private static void onTooltip(ItemStack stack,
                                  Item.TooltipContext ctx,
                                  TooltipType type,
                                  List<Text> lines) {
        if (stack == null || stack.isEmpty()) return;

        if (stack.isOf(Items.GLOW_INK_SAC)) {
            addBasicUnderName(lines, GLOW_SAC_HINT);
        }
        if (stack.isOf(Items.ECHO_SHARD)) {
            addBasicUnderName(lines, ECHO_SHARD_HINT);
        }
        if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
            addBasicUnderName(lines, Text.literal("Cooldown on use: 60s").formatted(Formatting.GRAY));
        }
        if (stack.getItem() instanceof MapBuilderItem) {
            lines.add(Text.empty());
            lines.add(Text.literal(" Right-Click: Set Top-Left Corner").formatted(Formatting.YELLOW));
            lines.add(Text.literal(" Left-Click: Set Bottom-Right Corner").formatted(Formatting.YELLOW));
            lines.add(Text.literal(" Right-Click in area: Build Map").formatted(Formatting.YELLOW));
            lines.add(Text.literal(" Shift + Z: Change Map Zoom").formatted(Formatting.YELLOW));
            lines.add(Text.empty());
            lines.add(Text.literal("Requires Empty Maps & Item Frames").formatted(Formatting.RED));
        }

        // Updated method call to handle all special trims
        appendSpecialTrimLines(stack, lines);
    }

    public static void addBasicUnderName(List<Text> lines, Text tip) {
        final int insertAt = Math.min(lines.size(), 1);
        if (insertAt < lines.size()) {
            Text existing = lines.get(insertAt);
            if (existing.getString().equals(tip.getString())) return;
            if (existing.getString().isBlank()) {
                lines.set(insertAt, tip);
                return;
            }
        }
        lines.add(insertAt, tip);
    }

    private static void appendSpecialTrimLines(ItemStack stack, List<Text> lines) {
        boolean emissive = stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false);
        boolean pulsing = stack.getOrDefault(ModDataComponents.PULSING_TRIM, false);

        // If the item has neither effect, do nothing
        if (!emissive && !pulsing) return;

        ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
        if (trim == null) return;

        // Choose the correct text line to add
        Text lineToAdd = emissive ? EMISSIVE_GLOW_LINE : PULSING_ECHO_LINE;

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

    private static int findUpgradeHeaderIndex(List<Text> lines) {
        for (int i = 0, n = lines.size(); i < n; i++) {
            String s = lines.get(i).getString();
            if (s == null) continue;
            s = s.trim();
            if (s.equalsIgnoreCase("Upgrade:") || s.equalsIgnoreCase("Upgrade")) {
                return i;
            }
        }
        return -1;
    }
}