package net.hallowed.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Path.of("config", "old-ways.client.json");

    private static ClientConfig CONFIG = new ClientConfig();

    public record Line(String text, int argb) {}
    private record Parsed(String tail, int argb) {}

    private ClientConfigManager() {}

    public static void load() {
        try {
            if (!Files.exists(FILE)) saveDefaults();
            try (Reader r = Files.newBufferedReader(FILE)) {
                ClientConfig read = GSON.fromJson(r, ClientConfig.class);
                CONFIG = (read != null) ? read : new ClientConfig();
            }
        } catch (IOException e) {
            CONFIG = new ClientConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(CONFIG, w);
            }
        } catch (IOException ignored) {}
    }

    private static void saveDefaults() throws IOException {
        Files.createDirectories(FILE.getParent());
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(new ClientConfig(), w);
        }
    }

    // ===== overlay reads =====
    public static boolean overlayEnabled() { return CONFIG.overlay != null && CONFIG.overlay.enabled; }
    public static boolean coordsVisible()  { return CONFIG.overlay == null || CONFIG.overlay.coordsVisible; }
    public static boolean timeVisible()    { return CONFIG.overlay == null || CONFIG.overlay.timeVisible; }

    public static String overlayPosition() {
        return (CONFIG.overlay != null && CONFIG.overlay.position != null) ? CONFIG.overlay.position : "top_left";
    }

    public static float overlayTextScale() {
        float s = (CONFIG.overlay != null) ? CONFIG.overlay.textScale : 1.0f;
        if (Float.isNaN(s) || s <= 0.05f) s = 1.0f;
        return Math.min(s, 10.0f);
    }

    // ===== overlay writes (used by inventory icons) =====
    public static void toggleCoordsVisible() { if (CONFIG.overlay != null) { CONFIG.overlay.coordsVisible = !CONFIG.overlay.coordsVisible; save(); } }
    public static void toggleTimeVisible()   { if (CONFIG.overlay != null) { CONFIG.overlay.timeVisible   = !CONFIG.overlay.timeVisible;   save(); } }

    public static void cyclePosition() {
        if (CONFIG.overlay == null) return;
        String p = overlayPosition();
        CONFIG.overlay.position = switch (p) {
            case "top_left" -> "top_right";
            case "top_right" -> "bottom_right";
            case "bottom_right" -> "bottom_left";
            default -> "top_left";
        };
        save();
    }

    public static boolean oldItemRenderingEnabled() {
        return CONFIG.oldItemRendering != null && CONFIG.oldItemRendering.enabled;
    }

    public static void cycleCoordsColor() {
        if (CONFIG.overlay == null) return;
        CONFIG.overlay.coordsFormat = cycleLeadingColor(CONFIG.overlay.coordsFormat);
        save();
    }

    public static void cycleTimeColor() {
        if (CONFIG.overlay == null) return;
        CONFIG.overlay.timeDayFormat = cycleLeadingColor(CONFIG.overlay.timeDayFormat);
        save();
    }

    // ===== line builders =====
    public static String ticksToHHMM(long dayTime) {
        long ticks = ((dayTime % 24000L) + 24000L) % 24000L;
        long adj = (ticks + 6000L) % 24000L;
        int hours = (int) (adj / 1000L);
        int minutes = (int) ((adj % 1000L) * 60L / 1000L);
        return String.format("%02d:%02d", hours, minutes);
    }

    public static Line buildCoords(double x, double y, double z) {
        String fmt = (CONFIG.overlay != null && CONFIG.overlay.coordsFormat != null) ? CONFIG.overlay.coordsFormat : "x: {x} | y: {y} | z: {z}";
        Parsed p = parseLeadingColor(fmt);
        String fx = String.format("%.2f", x), fy = String.format("%.2f", y), fz = String.format("%.2f", z);
        String text = p.tail.replace("{x}", fx).replace("{y}", fy).replace("{z}", fz);
        return new Line(text, p.argb);
    }

    public static Line buildTimeDay(String timeHHMM, int day) {
        String fmt = (CONFIG.overlay != null && CONFIG.overlay.timeDayFormat != null) ? CONFIG.overlay.timeDayFormat : "time: {time} | Day: {day}";
        Parsed p = parseLeadingColor(fmt);
        String text = p.tail.replace("{time}", timeHHMM).replace("{day}", Integer.toString(day));
        return new Line(text, p.argb);
    }

    // ===== color helpers =====
    private static Parsed parseLeadingColor(String s) {
        if (s != null && s.length() >= 2 && (s.charAt(0) == '&' || s.charAt(0) == '§')) {
            Integer rgb = mcColorCodeToRGB(Character.toLowerCase(s.charAt(1)));
            if (rgb != null) return new Parsed(s.substring(2), 0xFF000000 | rgb);
        }
        return new Parsed(s == null ? "" : s, 0xFFFFFFFF);
    }

    private static String cycleLeadingColor(String fmt) {
        // Colors cycle through this list:
        char[] order = new char[]{'f','6','b','a','c','e','9','d','2','3','4','5','7','8','0'};
        // Extract current or assume white
        char current = 'f';
        if (fmt != null && fmt.length() >= 2 && (fmt.charAt(0) == '&' || fmt.charAt(0) == '§')) current = Character.toLowerCase(fmt.charAt(1));
        // Find next
        int idx = 0;
        for (int i = 0; i < order.length; i++) if (order[i] == current) { idx = (i + 1) % order.length; break; }
        String tail = (fmt == null) ? "" : ( (fmt.length() >= 2 && (fmt.charAt(0)=='&'||fmt.charAt(0)=='§')) ? fmt.substring(2) : fmt );
        return "&" + order[idx] + tail;
    }

    private static Integer mcColorCodeToRGB(char code) {
        return switch (code) {
            case '0' -> 0x000000; case '1' -> 0x0000AA; case '2' -> 0x00AA00; case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000; case '5' -> 0xAA00AA; case '6' -> 0xFFAA00; case '7' -> 0xAAAAAA;
            case '8' -> 0x555555; case '9' -> 0x5555FF; case 'a' -> 0x55FF55; case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555; case 'd' -> 0xFF55FF; case 'e' -> 0xFFFF55; case 'f' -> 0xFFFFFF;
            default -> null;
        };
    }
}
