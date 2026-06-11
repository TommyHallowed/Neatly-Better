package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.util.PlaytimeStorage;
import net.minecraft.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {

    @Shadow @Final private LevelSummary summary;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private WorldSelectionList list;

    @Inject(method = "extractContent", at = @At("HEAD"))
    private void neatlybetter$renderPlaytime(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY,
            boolean hovered, float a, CallbackInfo ci
    ) {
        long ticks = PlaytimeStorage.get(summary);
        if (ticks <= 0L) return;

        String label = formatPlaytime(ticks);
        Component text = Component.literal(label).withStyle(ChatFormatting.GRAY);

        ObjectSelectionList.Entry<?> entry = (ObjectSelectionList.Entry<?>) (Object) this;
        int contentX = entry.getContentX();
        int contentY = entry.getContentY();

        int textWidth = this.minecraft.font.width(text);
        int x = contentX + this.list.getRowWidth() - textWidth - 5;
        int y = contentY + 1;

        graphics.text(this.minecraft.font, text, x, y, 0xFFAAAAAA);
    }

    @Unique
    private static String formatPlaytime(long ticks) {
        long totalMinutes = ticks / 20L / 60L;

        if (totalMinutes < 60L) {
            return Math.max(1L, totalMinutes) + "m";
        } else {
            double hours = totalMinutes / 60.0;
            String formatted = String.format("%.1f", hours);
            if (formatted.endsWith(".0")) {
                formatted = formatted.substring(0, formatted.length() - 2);
            }
            return formatted + "h";
        }
    }
}