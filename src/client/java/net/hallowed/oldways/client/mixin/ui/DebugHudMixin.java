package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.HudFormatting;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @ModifyArg(
            method = "render(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/DebugHud;drawText(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;Z)V",
                    ordinal = 0 // first call is the LEFT column
            ),
            index = 1
    )
    private List<String> oldways$rewriteLeftList(List<String> left) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        if (player == null) return left;

        boolean hasCompass = InventoryDeepScan.hasCompass(player) || EnderCheckClient.enderHasCompass();
        String coordsLine = hasCompass
                ? HudFormatting.coordsLine(player).text()
                : "you need compass to display coordinates";

        int xyzIdx = firstIndexStartingWith(left, "XYZ:");
        if (xyzIdx != -1) {
            left.set(xyzIdx, coordsLine);
        } else {
            int insertAt = Math.min(1, left.size());
            left.add(insertAt, coordsLine);
            xyzIdx = insertAt;
        }

        if (world == null) return left;

        boolean hasClock = InventoryDeepScan.hasClock(player) || EnderCheckClient.enderHasClock();
        String timeLine = hasClock
                ? HudFormatting.timeLine(world).text()
                : "you need clock to display time";

        int dayIdx = firstIndexStartingWith(left, "Day:");
        if (dayIdx != -1) {
            left.remove(dayIdx);
            if (dayIdx < xyzIdx) xyzIdx--;
        }

        int afterXYZ = Math.min(xyzIdx + 1, left.size());
        left.add(afterXYZ, timeLine);
        left.add(Math.min(afterXYZ + 1, left.size()), "");

        int diffIdx = firstIndexStartingWith(left, "Local Difficulty:");
        if (diffIdx != -1) {
            String s = left.get(diffIdx);
            int cut = s.indexOf(" (Day");
            if (cut != -1) left.set(diffIdx, s.substring(0, cut).trim());
        }

        return left;
    }

    @Unique
    private static int firstIndexStartingWith(List<String> lines, String prefix) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) return i;
        }
        return -1;
    }
}
