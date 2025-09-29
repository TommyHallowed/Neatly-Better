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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class ModTooltips {
    private ModTooltips() {}

    // Cache static lines once (no per-call allocations)
    private static final MutableText GLOW_SAC_HINT =
            Text.literal("Can be used on trimmed armor").formatted(Formatting.GRAY);
    private static final MutableText EMISSIVE_GLOW_LINE =
            Text.literal(" Glow Ink Sac").formatted(Formatting.AQUA);

    /** Call this from your client initializer */
    public static void init() {
        ItemTooltipCallback.EVENT.register(ModTooltips::onTooltip);
    }

    private static void onTooltip(ItemStack stack,
                                  Item.TooltipContext ctx,
                                  TooltipType type,
                                  List<Text> lines) {
        if (stack == null || stack.isEmpty()) return;

        // --- basic one-liners: show under the item name ---
        // (Fast early-outs; no string building unless needed)
        if (stack.isOf(Items.GLOW_INK_SAC)) {
            addBasicUnderName(lines, GLOW_SAC_HINT);
        }
        if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
            // dynamic value, so we construct this only when the item matches
            int secs = CommonConfigManager.totemCooldownSeconds();
            addBasicUnderName(lines, Text.literal("Cooldown on use: " + secs + "s").formatted(Formatting.GRAY));
        }

        // Keep your emissive-trim placement logic, but avoid reading TRIM unless flag is present
        appendEmissiveTrimLine(stack, lines);
    }

    /* ----------------- BASIC TOOLTIP HELPER ----------------- */

    public static void addBasicUnderName(List<Text> lines, Text tip) {
        // There is always at least the name line (index 0)
        final int insertAt = lines.size() > 1 ? 1 : Math.min(1, lines.size());

        if (insertAt < lines.size()) {
            Text existing = lines.get(insertAt);
            // Avoid duplicates if something already added the exact same string at line 2
            if (existing.getString().equals(tip.getString())) return;

            // Reuse blank spacer if present
            if (existing.getString().isBlank()) {
                lines.set(insertAt, tip);
                return;
            }
        }
        lines.add(insertAt, tip);
    }

    /* ----------------- YOUR OTHER FEATURES ----------------- */

    private static void appendEmissiveTrimLine(ItemStack stack, List<Text> lines) {
        // First check your boolean flag; only then read TRIM to avoid work
        if (!stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) return;

        ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
        if (trim == null) return;

        int up = findUpgradeHeaderIndex(lines);
        if (up >= 0) {
            // Skip exactly the two vanilla trim lines: pattern + material
            int idx = up + 1;
            int nonEmpty = 0;
            for (; idx < lines.size() && nonEmpty < 2; idx++) {
                if (!lines.get(idx).getString().isBlank()) nonEmpty++;
            }
            lines.add(idx, EMISSIVE_GLOW_LINE);
            return;
        }

        // Fallback: place near top but below the name if possible
        lines.add(Math.min(2, lines.size()), EMISSIVE_GLOW_LINE);
    }

    private static int findUpgradeHeaderIndex(List<Text> lines) {
        // Small, tight loop; break on first match
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
