package net.hallowed.oldways.client.feature.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.client.util.HudFormatting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public final class SmallHudOverlay {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private SmallHudOverlay() {}

    public static void render(DrawContext ctx) {
        if (MC.player == null || MC.world == null) return;
        if (MC.getDebugHud().shouldShowDebugHud()) return;

        PlayerEntity p = MC.player;
        ClientWorld w  = MC.world;

        boolean showCoords = HudFormatting.shouldShowCoords(p);
        boolean showTime   = HudFormatting.shouldShowTime(p);
        if (!showCoords && !showTime) return;

        var tr    = MC.textRenderer;
        float scale = 1.0f;
        int lineH   = tr.fontHeight;

        var coordsLine = showCoords ? HudFormatting.coordsLine(p) : null;
        var timeLine   = showTime   ? HudFormatting.timeLine(w)   : null;

        int coordsW = (coordsLine != null) ? tr.getWidth(coordsLine.text()) : 0;
        int timeW   = (timeLine   != null) ? tr.getWidth(timeLine.text())   : 0;

        int[] coordsXY = (coordsLine != null) ? HudFormatting.coordsXY(coordsW, lineH, scale) : null;
        int[] timeXY   = (timeLine   != null) ? HudFormatting.timeXY(timeW,   lineH, scale)   : null;

        int timeX = 0, timeY = 0;
        if (timeXY != null) { timeX = timeXY[0]; timeY = timeXY[1]; }

        if (coordsXY != null && timeXY != null) {
            String cp = HudFormatting.coordsPosition().toLowerCase();
            String tp = HudFormatting.timePosition().toLowerCase();
            if (cp.equals(tp)) {
                int gap = 2;
                if (tp.startsWith("top")) timeY = coordsXY[1] + lineH + gap;
                else                       timeY = coordsXY[1] - lineH - gap;
            }
        }

        var ms = ctx.getMatrices();
        ms.pushMatrix();
        ms.scale(scale, scale);

        if (coordsLine != null) {
            ctx.drawTextWithShadow(tr, coordsLine.text(),
                    (int)(coordsXY[0] / scale), (int)(coordsXY[1] / scale),
                    coordsLine.argb());
        }
        if (timeLine != null) {
            ctx.drawTextWithShadow(tr, timeLine.text(),
                    (int)(timeX / scale), (int)(timeY / scale),
                    timeLine.argb());
        }

        ms.popMatrix();
    }
}
