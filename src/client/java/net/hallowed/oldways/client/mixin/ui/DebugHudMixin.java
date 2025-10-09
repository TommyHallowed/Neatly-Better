package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.util.HudFormatting;
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

        List<String> lines = cir.getReturnValue();
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        ClientWorld w = mc.world;
        if (p == null) return;

        int xyzIdx;

            boolean hasCompass = InventoryDeepScan.hasCompass(p) || EnderCheckClient.enderHasCompass();
            String coordsLine = hasCompass ? HudFormatting.coordsLine(p).text() : "you need compass to display coordinates";
            int idx = firstIndexStartingWith(lines, "XYZ:");
            if (idx != -1) {
                lines.set(idx, coordsLine);
                xyzIdx = idx;
            } else {
                int insert = Math.min(1, lines.size());
                lines.add(insert, coordsLine);
                xyzIdx = insert;
            }

            if (w == null) {
                cir.setReturnValue(lines);
                return;
            }
            boolean hasClock = InventoryDeepScan.hasClock(p) || EnderCheckClient.enderHasClock();
            String timeLine = hasClock ? HudFormatting.timeLine(w).text() : "you need clock to display time";

            int dayIdx = firstIndexStartingWith(lines, "Day:");
            if (dayIdx != -1) {
                lines.remove(dayIdx);
                if (dayIdx < xyzIdx) xyzIdx--;
            }

            int insertAt = (xyzIdx != -1) ? Math.min(xyzIdx + 1, lines.size()) : Math.min(1, lines.size());
            lines.add(insertAt, timeLine);
            lines.add(insertAt + 1, "");

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
