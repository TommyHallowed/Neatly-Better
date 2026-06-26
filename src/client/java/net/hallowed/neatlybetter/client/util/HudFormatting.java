package net.hallowed.neatlybetter.client.util;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;


public final class HudFormatting {
    private HudFormatting() {}

    private static final Minecraft MC = Minecraft.getInstance();

    public record Line(String text, int argb) {}
    private record Parsed(String tail, int argb) {}

    private static double lastCoordX, lastCoordY, lastCoordZ;
    private static int lastCoordColorBucket = Integer.MIN_VALUE;
    private static String lastCoordsFormat;
    private static Line cachedCoords;

    private static String lastTimeKey;
    private static String lastTimeFormat;
    private static Line cachedTime;

    private static int   lastScreenW = -1, lastScreenH = -1;
    private static float lastScale   = -1f;
    private static String lastCoordsPos = null, lastTimePos = null;
    private static int[] cachedCoordsXY, cachedTimeXY;

    private static int secondBucket(long timeOfDay) { return (int)((timeOfDay % 24000L) / 20L); }

    public static boolean shouldShowCoords(Player p) {
        return NTClientConfig.CONFIG.showCoords.get()
                && (InventoryDeepScan.hasCompass(p)
                || EnderCheckClient.enderHasCompass());
    }
    public static boolean shouldShowTime(Player p) {
        return NTClientConfig.CONFIG.showTime.get()
                && (InventoryDeepScan.hasClock(p)
                || EnderCheckClient.enderHasClock());
    }

    public static Line coordsLine(Player p) {
        final double x = p.getX(), y = p.getY(), z = p.getZ();
        final ClientLevel w = MC.level;
        final int colorBucket = (w != null) ? secondBucket(w.getDefaultClockTime()) : 0;
        final String currentFormat = NTClientConfig.CONFIG.coordsFormat.get();

        if (cachedCoords == null
                || movedEnough(x, y, z)
                || colorBucket != lastCoordColorBucket
                || !currentFormat.equals(lastCoordsFormat)) {
            lastCoordX = x; lastCoordY = y; lastCoordZ = z;
            lastCoordColorBucket = colorBucket;
            lastCoordsFormat = currentFormat;
            cachedCoords = buildCoords(x, y, z);
        }
        return cachedCoords;
    }

    public static Line timeLine(ClientLevel w) {
        String hhmm = ticksToHHMM(w.getDefaultClockTime());
        int    day  = (int)(w.getGameTime() / 24000L);
        int    bucket = secondBucket(w.getDefaultClockTime());
        String currentFormat = NTClientConfig.CONFIG.timeDayFormat.get();
        String key = hhmm + "|" + day + "|" + bucket;

        if (cachedTime == null
                || !key.equals(lastTimeKey)
                || !currentFormat.equals(lastTimeFormat)) {
            lastTimeKey = key;
            lastTimeFormat = currentFormat;
            cachedTime = buildTimeDay(hhmm, day);
        }
        return cachedTime;
    }

    private static boolean movedEnough(double x, double y, double z) {
        double dx = x - lastCoordX, dy = y - lastCoordY, dz = z - lastCoordZ;
        return (dx*dx + dy*dy + dz*dz) > (0.05 * 0.05); //refresh rate of coords
    }

    public static int[] coordsXY(int textW, int lineH, float scale) { return ensureAnchor(textW, lineH, scale, true); }
    public static int[] timeXY  (int textW, int lineH, float scale) { return ensureAnchor(textW, lineH, scale, false); }

    private static int[] ensureAnchor(int textW, int lineH, float scale, boolean forCoords) {
        int screenW = MC.getWindow().getGuiScaledWidth();
        int screenH = MC.getWindow().getGuiScaledHeight();
        String pos  = forCoords ? coordsPosition() : timePosition();

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

    private static boolean notEq(String a, String b) { return a == null || !a.equalsIgnoreCase(b); }
    private static int[] anchorFor(String pos, int width, int height, int sw, int sh, float scale) {
        int pad = 4;
        int w = (int)(width * scale), h = (int)(height * scale);
        String p = (pos == null) ? "top_left" : pos.toLowerCase();
        return switch (p) {
            case "top_right"    -> new int[]{sw - w - pad, pad};
            case "bottom_left"  -> new int[]{pad, sh - h - pad};
            case "bottom_right" -> new int[]{sw - w - pad, sh - h - pad};
            default             -> new int[]{pad, pad};
        };
    }

    public static String ticksToHHMM(long dayTime) {
        long ticks = ((dayTime % 24000L) + 24000L) % 24000L;
        long adj = (ticks + 6000L) % 24000L;
        int hours = (int) (adj / 1000L);
        int minutes = (int) ((adj % 1000L) * 60L / 1000L);
        return String.format("%02d:%02d", hours, minutes);
    }

    public static Line buildCoords(double x, double y, double z) {
        Parsed p = parseLeadingColor(NTClientConfig.CONFIG.coordsFormat.get());
        String fx = String.format("%.2f", x), fy = String.format("%.2f", y), fz = String.format("%.2f", z);
        String text = p.tail.replace("{x}", fx).replace("{y}", fy).replace("{z}", fz);
        return new Line(text, p.argb);
    }

    public static Line buildTimeDay(String timeHHMM, int day) {
        Parsed p = parseLeadingColor(NTClientConfig.CONFIG.timeDayFormat.get());
        String text = p.tail.replace("{time}", timeHHMM).replace("{day}", Integer.toString(day));
        return new Line(text, p.argb);
    }

    public static String coordsPosition() { return NTClientConfig.CONFIG.coordsPos.get().name().toLowerCase(); }
    public static String timePosition()   { return NTClientConfig.CONFIG.timePos.get().name().toLowerCase(); }

    private static Parsed parseLeadingColor(String s) {
        if (s != null && s.length() >= 2 && (s.charAt(0) == '&' || s.charAt(0) == '§')) {
            Integer rgb = mcColorCodeToRGB(Character.toLowerCase(s.charAt(1)));
            if (rgb != null) return new Parsed(s.substring(2), 0xFF000000 | rgb);
        }
        return new Parsed(s == null ? "" : s, 0xFFFFFFFF);
    }

    private static Integer mcColorCodeToRGB(char code) {
        return switch (code) {
            case '0' -> 0x000000; case '1' -> 0x0000AA; case '2' -> 0x00AA00; case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000; case '5' -> 0xAA00AA; case '6' -> 0xFFAA00; case '7' -> 0xAAAAAA;
            case '8' -> 0x555555; case '9' -> 0x5555FF; case 'a' -> 0x55FF55; case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555; case 'd' -> 0xFF55FF; case 'e' -> 0xFFFF55; case 'f' -> 0xFFFFFF;
            default  -> null;
        };
    }
}
