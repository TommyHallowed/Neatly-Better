// src/client/java/net/hallowed/oldways/client/mixin/ui/DebugHudMixin.java
package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.ui.HudFormatting;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Inject(method = "getLeftText()Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void oldways$coordsFirst_timeUnder(CallbackInfoReturnable<List<String>> cir) {
        if (!ClientConfigManager.overlayEnabled()) return;

        List<String> lines = cir.getReturnValue();
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        ClientWorld w = mc.world;
        if (p == null || w == null) return;

        // Deep inventory (TTL-cached) OR ender flags
        boolean hasCompass = InventoryDeepScan.hasCompass(p) || EnderCheckClient.enderHasCompass();
        boolean hasClock   = InventoryDeepScan.hasClock(p)   || EnderCheckClient.enderHasClock();

        String coordsLine = hasCompass
                ? HudFormatting.coordsLine(p).text()
                : "you need compass to display coordinates";

        String timeLine = hasClock
                ? HudFormatting.timeLine(w).text()
                : "you need clock to display time";

        // Replace XYZ with our coords (or insert near the top)
        int xyzIdx = firstIndexStartingWith(lines, "XYZ:");
        if (xyzIdx != -1) {
            lines.set(xyzIdx, coordsLine);
        } else {
            xyzIdx = Math.min(1, lines.size());
            lines.add(xyzIdx, coordsLine);
        }

        // Remove vanilla "Day:" to avoid duplicate / "Day 0"
        int dayIdx = firstIndexStartingWith(lines, "Day:");
        if (dayIdx != -1) {
            lines.remove(dayIdx);
            if (dayIdx < xyzIdx) xyzIdx--;
        }

        // Insert time directly under coords + spacer
        int insertAt = Math.min(xyzIdx + 1, lines.size());
        lines.add(insertAt, timeLine);
        lines.add(insertAt + 1, "");

        // Strip "(Day N)" from Local Difficulty line
        int diffIdx = firstIndexStartingWith(lines, "Local Difficulty:");
        if (diffIdx != -1) {
            String s = lines.get(diffIdx);
            int cut = s.indexOf(" (Day");
            if (cut != -1) lines.set(diffIdx, s.substring(0, cut).trim());
        }

        cir.setReturnValue(lines);
    }

    @Unique
    private static int firstIndexStartingWith(List<String> lines, String prefix) {
        for (int i = 0; i < lines.size(); i++) if (lines.get(i).startsWith(prefix)) return i;
        return -1;
    }
}
