package net.hallowed.neatlybetter.client.feature.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.util.HudFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class SmallHudOverlay implements HudElement {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final SmallHudOverlay INSTANCE = new SmallHudOverlay();

    private SmallHudOverlay() {}

    public static SmallHudOverlay getInstance() {
        return INSTANCE;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        if (!NTClientConfig.CONFIG.showCoords.get() && !NTClientConfig.CONFIG.showTime.get()) return;

        if (MC.player == null || MC.level == null) return;
        if (MC.getDebugOverlay().showDebugScreen()) return;

        Player p = MC.player;
        ClientLevel w = MC.level;

        boolean showCoords = HudFormatting.shouldShowCoords(p);
        boolean showTime = HudFormatting.shouldShowTime(p);
        if (!showCoords && !showTime) return;

        var tr = MC.font;
        float scale = 1.0f;
        int lineH = tr.lineHeight;

        var coordsLine = showCoords ? HudFormatting.coordsLine(p) : null;
        var timeLine = showTime ? HudFormatting.timeLine(w) : null;

        int coordsW = (coordsLine != null) ? tr.width(coordsLine.text()) : 0;
        int timeW = (timeLine != null) ? tr.width(timeLine.text()) : 0;

        int[] coordsXY = (coordsLine != null) ? HudFormatting.coordsXY(coordsW, lineH, scale) : null;
        int[] timeXY = (timeLine != null) ? HudFormatting.timeXY(timeW, lineH, scale) : null;

        int timeX = 0, timeY = 0;
        if (timeXY != null) {
            timeX = timeXY[0];
            timeY = timeXY[1];
        }

        if (coordsXY != null && timeXY != null) {
            String cp = HudFormatting.coordsPosition().toLowerCase();
            String tp = HudFormatting.timePosition().toLowerCase();
            if (cp.equals(tp)) {
                int gap = 2;
                if (tp.startsWith("top")) timeY = coordsXY[1] + lineH + gap;
                else timeY = coordsXY[1] - lineH - gap;
            }
        }

        var ms = graphics.pose();
        ms.pushMatrix();
        ms.scale(scale, scale);

        if (coordsLine != null) {
            assert coordsXY != null;
            graphics.text(tr, coordsLine.text(),
                    (int)(coordsXY[0] / scale), (int)(coordsXY[1] / scale),
                    coordsLine.argb());
        }
        if (timeLine != null) {
            graphics.text(tr, timeLine.text(),
                    (int)(timeX / scale), (int)(timeY / scale),
                    timeLine.argb());
        }

        ms.popMatrix();
    }
}