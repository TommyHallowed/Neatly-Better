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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

@Environment(EnvType.CLIENT)
public final class SmallHudOverlay implements HudRenderCallback {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public static void register() {
        HudRenderCallback.EVENT.register(new SmallHudOverlay());
    }

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!ClientConfigManager.overlayEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.getDebugHud().shouldShowDebugHud()) return;

        PlayerEntity p = mc.player;
        ClientWorld w = mc.world;

        // ...
        boolean hasCompass = (ClientConfigManager.coordsVisible())
                && (invHas(p, Items.COMPASS) || EnderCheckClient.enderHasCompass());

        boolean hasClock   = (ClientConfigManager.timeVisible())
                && (invHas(p, Items.CLOCK)   || EnderCheckClient.enderHasClock());
// ...


        if (!hasCompass && !hasClock) return;

        // Build lines (each may carry its own color from leading &x)
        ClientConfigManager.Line coords = hasCompass
                ? ClientConfigManager.buildCoords(p.getX(), p.getY(), p.getZ()) : null;
        ClientConfigManager.Line time   = hasClock
                ? ClientConfigManager.buildTimeDay(
                ClientConfigManager.ticksToHHMM(w.getTimeOfDay()),
                (int) (w.getTime() / 24000L)) : null;

        var tr = mc.textRenderer;
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        float scale = ClientConfigManager.overlayTextScale();

        String widthRef = coords != null ? coords.text() : time.text();
        int baseWidth = tr.getWidth(widthRef);
        int linesCount = (coords != null ? 1 : 0) + (time != null ? 1 : 0);
        int blockH = tr.fontHeight * linesCount + (linesCount >= 2 ? 12 : 0);

        int x, y;
        switch (ClientConfigManager.overlayPosition().toLowerCase()) {
            case "top_right" -> { x = (int)(screenW - baseWidth * scale - 4); y = 4; }
            case "bottom_left" -> { x = 4; y = (int)(screenH - blockH * scale); }
            case "bottom_right" -> { x = (int)(screenW - baseWidth * scale - 4); y = (int)(screenH - blockH * scale); }
            default -> { x = 4; y = 4; } // top_left
        }

        Matrix3x2fStack m = ctx.getMatrices();
        m.pushMatrix();
        m.scale(scale, scale);

        int sx = (int)(x / scale);
        int sy = (int)(y / scale);

        int dy = 0;
        if (coords != null) {
            ctx.drawTextWithShadow(tr, Text.literal(coords.text()), sx, sy, coords.argb());
            dy += tr.fontHeight + 2;
        }
        if (time != null) {
            ctx.drawTextWithShadow(tr, Text.literal(time.text()), sx, sy + dy, time.argb());
        }

        m.popMatrix();
    }

    /** Only checks visible inventory; ender chest comes from EnderCheckClient flags. */
    private static boolean invHas(PlayerEntity player, net.minecraft.item.Item item) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && s.isOf(item)) return true;
        }
        return false;
    }
}
