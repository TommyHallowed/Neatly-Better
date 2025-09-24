package net.hallowed.oldways.client.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

@Environment(EnvType.CLIENT)
public final class SmallHudOverlay implements HudRenderCallback {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public static void register() { HudRenderCallback.EVENT.register(new SmallHudOverlay()); }

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!ClientConfigManager.overlayEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.getDebugHud().shouldShowDebugHud()) return;

        PlayerEntity p = mc.player;
        ClientWorld w = mc.world;

        boolean showCoords = ClientConfigManager.coordsVisible()
                && (p.getInventory().contains(Items.COMPASS.getDefaultStack()) || EnderCheckClient.enderHasCompass());
        boolean showTime   = ClientConfigManager.timeVisible()
                && (p.getInventory().contains(Items.CLOCK.getDefaultStack())   || EnderCheckClient.enderHasClock());
        if (!showCoords && !showTime) return;

        var tr = mc.textRenderer;
        float scale = ClientConfigManager.overlayTextScale();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        ClientConfigManager.Line coordsLine = showCoords
                ? ClientConfigManager.buildCoords(p.getX(), p.getY(), p.getZ()) : null;
        ClientConfigManager.Line timeLine   = showTime
                ? ClientConfigManager.buildTimeDay(ClientConfigManager.ticksToHHMM(w.getTimeOfDay()), (int)(w.getTime() / 24000L)) : null;

        int coordsW = (coordsLine != null) ? tr.getWidth(coordsLine.text()) : 0;
        int timeW   = (timeLine   != null) ? tr.getWidth(timeLine.text())   : 0;
        int lineH   = tr.fontHeight;

        int[] coordsXY = (coordsLine != null)
                ? anchorFor(ClientConfigManager.coordsPosition(), coordsW, lineH, screenW, screenH, scale)
                : null;
        int[] timeXY = (timeLine != null)
                ? anchorFor(ClientConfigManager.timePosition(), timeW, lineH, screenW, screenH, scale)
                : null;

        if (coordsXY != null && timeXY != null &&
                ClientConfigManager.coordsPosition().equalsIgnoreCase(ClientConfigManager.timePosition())) {
            int gap = 2;
            String corner = ClientConfigManager.timePosition().toLowerCase();
            if (corner.startsWith("top")) timeXY[1] += lineH + gap;
            else                          timeXY[1] -= lineH + gap;
        }

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(scale, scale);

        if (coordsLine != null) {
            ctx.drawTextWithShadow(tr, coordsLine.text(), (int)(coordsXY[0] / scale), (int)(coordsXY[1] / scale), coordsLine.argb());
        }
        if (timeLine != null) {
            ctx.drawTextWithShadow(tr, timeLine.text(), (int)(timeXY[0] / scale), (int)(timeXY[1] / scale), timeLine.argb());
        }

        ctx.getMatrices().popMatrix();
    }

    private static int[] anchorFor(String pos, int width, int height, int screenW, int screenH, float scale) {
        int pad = 4;
        int w = (int)(width * scale), h = (int)(height * scale);
        return switch (pos.toLowerCase()) {
            case "top_right"    -> new int[]{screenW - w - pad, pad};
            case "bottom_left"  -> new int[]{pad, screenH - h - pad};
            case "bottom_right" -> new int[]{screenW - w - pad, screenH - h - pad};
            default             -> new int[]{pad, pad}; // top_left
        };
    }
}
