package net.hallowed.oldways.client.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.hallowed.oldways.content.ModDataComponents;
import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
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

        appendEmissiveTrimLine(stack, lines);
        appendTotemCooldownLine(stack, lines);
    }

    /* ----------------- FEATURES ----------------- */

    private static void appendEmissiveTrimLine(ItemStack stack, List<Text> lines) {
        // Only for trimmed armor with our emissive flag
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
            // Insert directly after the trim lines (no extra blank line)
            lines.add(idx, glowLine);
            return;
        }

        // Fallback if somehow no Upgrade block exists
        lines.add(Math.min(2, lines.size()), glowLine);
    }

    private static void appendTotemCooldownLine(ItemStack stack, List<Text> lines) {
        if (!stack.isOf(Items.TOTEM_OF_UNDYING)) return;
        int secs = CommonConfigManager.totemCooldownSeconds();
        lines.add(Text.literal("Cooldown on use: " + secs + "s").formatted(Formatting.GRAY));
    }

    /* ----------------- helpers ----------------- */

    private static int findUpgradeHeaderIndex(List<Text> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).getString().trim();
            if (s.equalsIgnoreCase("Upgrade:") || s.equalsIgnoreCase("Upgrade")) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isGreen(Text t) {
        TextColor c = t.getStyle().getColor();
        Integer g  = Formatting.GREEN.getColorValue();
        Integer dg = Formatting.DARK_GREEN.getColorValue();
        return c != null && (c.getRgb() == (g != null ? g : -1) || c.getRgb() == (dg != null ? dg : -1));
    }
}
