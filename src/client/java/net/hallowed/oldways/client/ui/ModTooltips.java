package net.hallowed.oldways.client.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.hallowed.oldways.config.CommonConfigManager;
import net.hallowed.oldways.content.ModDataComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class ModTooltips {
    private ModTooltips() {}

    /** Call this from your client initializer */
    public static void init() {
        ItemTooltipCallback.EVENT.register(ModTooltips::onTooltip);
    }

    private static void onTooltip(ItemStack stack,
                                  Item.TooltipContext ctx,
                                  TooltipType type,
                                  List<Text> lines) {
        if (stack == null) return;

        // --- basic one-liners: show under the item name ---
        if (stack.isOf(Items.GLOW_INK_SAC)) {
            addBasicUnderName(lines, Text.literal("Can be used on trimmed armor").formatted(Formatting.GRAY));
        }
        if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
            int secs = CommonConfigManager.totemCooldownSeconds();
            addBasicUnderName(lines, Text.literal("Cooldown on use: " + secs + "s").formatted(Formatting.GRAY));
        }

        // feature-specific placement (keeps your original logic)
        appendEmissiveTrimLine(stack, lines);
    }

    /* ----------------- BASIC TOOLTIP HELPER ----------------- */

    public static void addBasicUnderName(List<Text> lines, Text tip) {
        // There is always at least the name line (index 0), but guard anyway.
        final int idx = lines.isEmpty() ? 0 : 1;

        if (idx < lines.size()) {
            Text existing = lines.get(idx);
            // Avoid duplicates if something already added the exact same text at line 2
            if (existing.getString().equals(tip.getString())) return;

            // If vanilla/mod leaves a blank spacer on line 2, reuse it instead of inserting
            if (existing.getString().isBlank()) {
                lines.set(idx, tip);
                return;
            }
        }
        // Either no line 2 yet (size==1) or a real line exists—insert and shift the rest
        lines.add(idx, tip);
    }

    /* ----------------- YOUR OTHER FEATURES ----------------- */

    private static void appendEmissiveTrimLine(ItemStack stack, List<Text> lines) {
        ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
        if (trim == null) return;
        if (!stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) return;

        Text glowLine = Text.literal(" Glow Ink Sac").formatted(Formatting.AQUA);

        int up = findUpgradeHeaderIndex(lines);
        if (up >= 0) {
            // Skip exactly the two vanilla trim lines: pattern + material
            int idx = up + 1;
            int nonEmpty = 0;
            while (idx < lines.size() && nonEmpty < 2) {
                if (!lines.get(idx).getString().isBlank()) nonEmpty++;
                idx++;
            }
            lines.add(idx, glowLine);
            return;
        }

        // Fallback: place near top but below the name if possible
        lines.add(Math.min(2, lines.size()), glowLine);
    }

    private static int findUpgradeHeaderIndex(List<Text> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).getString().trim();
            if (s.equalsIgnoreCase("Upgrade:") || s.equalsIgnoreCase("Upgrade")) {
                return i;
            }
        }
        return -1;
    }
}
