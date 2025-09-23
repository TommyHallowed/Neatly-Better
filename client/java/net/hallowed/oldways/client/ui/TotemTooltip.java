package net.hallowed.oldways.client.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.hallowed.oldways.config.CommonConfigManager; // <-- ADD
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public final class TotemTooltip {
    private TotemTooltip() {}

    public static void register() {
        // Keep params typeless to avoid mapping differences
        ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
            if (stack != null && stack.isOf(Items.TOTEM_OF_UNDYING)) {
                int secs = CommonConfigManager.totemCooldownSeconds();
                lines.add(Text.literal("Cooldown on use: " + secs + "s")
                        .formatted(Formatting.GRAY));
            }
        });
    }
}
