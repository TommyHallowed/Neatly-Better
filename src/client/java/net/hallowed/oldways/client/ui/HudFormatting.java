// src/client/java/net/hallowed/oldways/client/ui/HudFormatting.java
package net.hallowed.oldways.client.ui;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.hallowed.oldways.client.util.EnderCheckClient;
import net.hallowed.oldways.client.util.InventoryDeepScan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;


public final class HudFormatting {
    private HudFormatting() {}

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // ---- knobs ----
    /** Rebuild coords text when movement since last build exceeds this distance (in blocks). */
    private static final double COORD_REFRESH_EPS = 0.05; // ~5 cm

    /** 20 ticks = 1s bucket; any color rule that depends on time/day will update within 1s. */
    private static int secondBucket(long timeOfDay) { return (int)((timeOfDay % 24000L) / 20L); }

    // ---- coords cache ----
    private static double lastCoordX, lastCoordY, lastCoordZ; // last position we formatted at
    private static int    lastCoordColorBucket = Integer.MIN_VALUE;
    private static ClientConfigManager.Line cachedCoords;

    // ---- time cache ----
    private static String lastTimeKey = null; // "HH:MM|day|bucket"
    private static ClientConfigManager.Line cachedTime;

    // ---- layout cache ----
    private static int   lastScreenW = -1, lastScreenH = -1;
    private static float lastScale   = -1f;
    private static String lastCoordsPos = null, lastTimePos = null;
    private static int[] cachedCoordsXY, cachedTimeXY;

    /* ================= visibility checks ================= */

    public static boolean shouldShowCoords(PlayerEntity p) {
        return ClientConfigManager.coordsVisible()
                && (InventoryDeepScan.hasCompass(p) || EnderCheckClient.enderHasCompass());
    }

    public static boolean shouldShowTime(PlayerEntity p) {
        return ClientConfigManager.timeVisible()
                && (InventoryDeepScan.hasClock(p) || EnderCheckClient.enderHasClock());
    }

    /* ================= formatted text ================= */

    /** Cached coords line; updates when you move more than EPS or when the per-second color “bucket” changes. */
    public static ClientConfigManager.Line coordsLine(PlayerEntity p) {
        final double x = p.getX(), y = p.getY(), z = p.getZ();
        final ClientWorld w = MC.world;
        final int colorBucket = (w != null) ? secondBucket(w.getTimeOfDay()) : 0;

        if (cachedCoords == null || movedEnough(x, y, z) || colorBucket != lastCoordColorBucket) {
            lastCoordX = x; lastCoordY = y; lastCoordZ = z;
            lastCoordColorBucket = colorBucket;
            cachedCoords = ClientConfigManager.buildCoords(x, y, z);
        }
        return cachedCoords;
    }

    /** Cached time/day line; updates on minute/day change OR on per-second bucket for color changes. */
    public static ClientConfigManager.Line timeLine(ClientWorld w) {
        String hhmm = ClientConfigManager.ticksToHHMM(w.getTimeOfDay());
        int    day  = (int)(w.getTime() / 24000L);
        int    bucket = secondBucket(w.getTimeOfDay());
        String key = hhmm + "|" + day + "|" + bucket;
        if (cachedTime == null || !key.equals(lastTimeKey)) {
            lastTimeKey = key;
            cachedTime  = ClientConfigManager.buildTimeDay(hhmm, day);
        }
        return cachedTime;
    }

    private static boolean movedEnough(double x, double y, double z) {
        double dx = x - lastCoordX, dy = y - lastCoordY, dz = z - lastCoordZ;
        return (dx*dx + dy*dy + dz*dz) > (COORD_REFRESH_EPS * COORD_REFRESH_EPS);
    }

    /* ================= anchored positions (cached) ================= */

    public static int[] coordsXY(int textW, int lineH, float scale) { return ensureAnchor(textW, lineH, scale, true); }
    public static int[] timeXY  (int textW, int lineH, float scale) { return ensureAnchor(textW, lineH, scale, false); }

    private static int[] ensureAnchor(int textW, int lineH, float scale, boolean forCoords) {
        int screenW = MC.getWindow().getScaledWidth();
        int screenH = MC.getWindow().getScaledHeight();
        String pos  = forCoords ? ClientConfigManager.coordsPosition() : ClientConfigManager.timePosition();

        boolean needFull = (screenW != lastScreenW) || (screenH != lastScreenH)
                || (scale != lastScale)
                || (forCoords ? notEq(pos, lastCoordsPos) : notEq(pos, lastTimePos))
                || (forCoords ? cachedCoordsXY == null : cachedTimeXY == null);

        if (needFull) {
            lastScreenW = screenW; lastScreenH = screenH; lastScale = scale;
            if (forCoords) lastCoordsPos = pos; else lastTimePos = pos;

            int[] xy = anchorFor(pos, textW, lineH, screenW, screenH, scale);
            if (forCoords) cachedCoordsXY = xy; else cachedTimeXY = xy;
        } else {
            // Right-anchored X depends on width; re-evaluate X only
            switch (pos.toLowerCase()) {
                case "top_right", "bottom_right" -> {
                    int wpx = (int)(textW * scale);
                    if (forCoords) cachedCoordsXY[0] = screenW - wpx - 4;
                    else           cachedTimeXY[0]   = screenW - wpx - 4;
                }
                default -> {}
            }
        }
        return forCoords ? cachedCoordsXY : cachedTimeXY;
    }

    private static boolean notEq(String a, String b) {
        if (a == null || b == null) return true;
        return !a.equalsIgnoreCase(b);
    }

    private static int[] anchorFor(String pos, int width, int height, int screenW, int screenH, float scale) {
        int pad = 4;
        int w = (int)(width * scale), h = (int)(height * scale);
        String p = (pos == null) ? "top_left" : pos.toLowerCase();
        return switch (p) {
            case "top_right"    -> new int[]{screenW - w - pad, pad};
            case "bottom_left"  -> new int[]{pad, screenH - h - pad};
            case "bottom_right" -> new int[]{screenW - w - pad, screenH - h - pad};
            default             -> new int[]{pad, pad}; // top_left
        };
    }
}
