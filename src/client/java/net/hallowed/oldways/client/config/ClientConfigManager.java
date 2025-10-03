package net.hallowed.oldways.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ClientConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Path.of("config", "old-ways.client.json");

    private static ClientConfig CONFIG = new ClientConfig();

    public record Line(String text, int argb) {}
    private record Parsed(String tail, int argb) {}

    private ClientConfigManager() {}

    public static void load() {
        try {
            Files.createDirectories(FILE.getParent());
            String beforeJson = null;
            ClientConfig loaded = null;
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                    loaded = GSON.fromJson(r, ClientConfig.class);
                } catch (Exception ignored) {}
                if (Files.exists(FILE)) {
                    beforeJson = Files.readString(FILE, StandardCharsets.UTF_8);
                }
            }
            ClientConfig defaults = new ClientConfig();
            ClientConfig merged = mergeWithDefaults(defaults, loaded);
            CONFIG = merged;
            String afterJson = GSON.toJson(merged);
            if (!Files.exists(FILE) || !Objects.equals(normalize(beforeJson), normalize(afterJson))) {
                save();
            }
        } catch (IOException ignored) {
            CONFIG = new ClientConfig();
        }
    }

    private static ClientConfig mergeWithDefaults(ClientConfig defaults, ClientConfig incoming) {
        if (incoming == null) return defaults;
        JsonObject defObj = GSON.toJsonTree(defaults).getAsJsonObject();
        JsonObject inObj  = GSON.toJsonTree(incoming).getAsJsonObject();
        deepMergeObjects(inObj, defObj);
        return GSON.fromJson(defObj, ClientConfig.class);
    }

    private static void deepMergeObjects(JsonObject src, JsonObject dst) {
        for (var entry : src.entrySet()) {
            String key = entry.getKey();
            JsonElement srcVal = entry.getValue();
            if (!dst.has(key)) {
                dst.add(key, srcVal);
                continue;
            }
            JsonElement dstVal = dst.get(key);
            if (srcVal != null && srcVal.isJsonObject() && dstVal != null && dstVal.isJsonObject()) {
                deepMergeObjects(srcVal.getAsJsonObject(), dstVal.getAsJsonObject());
            } else {
                dst.add(key, srcVal);
            }
        }
    }

    private static String normalize(String json) {
        return json == null ? null : json.replace("\r\n", "\n").trim();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(CONFIG, w);
            }
        } catch (IOException ignored) {}
    }

    public static void reload() { load(); }

    public static boolean stuckProjectilesEnabled() {
        return CONFIG.stuckProjectiles == null || CONFIG.stuckProjectiles.enabled;
    }
    public static void setStuckProjectilesEnabled(boolean enabled) {
        if (CONFIG.stuckProjectiles == null) CONFIG.stuckProjectiles = new ClientConfig.StuckProjectiles();
        CONFIG.stuckProjectiles.enabled = enabled;
        save();
    }
    public static void toggleStuckProjectiles() { setStuckProjectilesEnabled(!stuckProjectilesEnabled()); }

    public static boolean oldItemRenderingEnabled() {
        return CONFIG.oldFeatures != null && CONFIG.oldFeatures.oldItemRendering;
    }
    public static boolean potionGlintEnabled() {
        return CONFIG.oldFeatures == null || CONFIG.oldFeatures.potionGlint;
    }

    public static boolean overlayEnabled() { return CONFIG.overlay != null && CONFIG.overlay.enabled; }
    public static boolean coordsVisible()  { return CONFIG.overlay == null || CONFIG.overlay.coordsVisible; }
    public static boolean timeVisible()    { return CONFIG.overlay == null || CONFIG.overlay.timeVisible; }

    public static void toggleCoordsVisible() {
        if (CONFIG.overlay == null) CONFIG.overlay = new ClientConfig.Overlay();
        CONFIG.overlay.coordsVisible = !CONFIG.overlay.coordsVisible;
        save();
    }
    public static void toggleTimeVisible() {
        if (CONFIG.overlay == null) CONFIG.overlay = new ClientConfig.Overlay();
        CONFIG.overlay.timeVisible = !CONFIG.overlay.timeVisible;
        save();
    }

    private static String fallbackPos() {
        return (CONFIG.overlay != null && CONFIG.overlay.position != null) ? CONFIG.overlay.position : "top_left";
    }
    public static String coordsPosition() {
        return (CONFIG.overlay != null && CONFIG.overlay.coordsPosition != null) ? CONFIG.overlay.coordsPosition : fallbackPos();
    }
    public static String timePosition() {
        return (CONFIG.overlay != null && CONFIG.overlay.timePosition != null) ? CONFIG.overlay.timePosition : fallbackPos();
    }
    public static void cycleCoordsPosition() {
        if (CONFIG.overlay == null) CONFIG.overlay = new ClientConfig.Overlay();
        CONFIG.overlay.coordsPosition = nextCorner(coordsPosition());
        save();
    }
    public static void cycleTimePosition() {
        if (CONFIG.overlay == null) CONFIG.overlay = new ClientConfig.Overlay();
        CONFIG.overlay.timePosition = nextCorner(timePosition());
        save();
    }
    private static String nextCorner(String p) {
        return switch (p.toLowerCase()) {
            case "top_left" -> "top_right";
            case "top_right" -> "bottom_right";
            case "bottom_right" -> "bottom_left";
            default -> "top_left";
        };
    }

    public static boolean autoCloseRecipeBookEnabled() {
        return CONFIG.recipeBook != null && CONFIG.recipeBook.autoClose;
    }
    public static boolean recipeBookHideButtonEnabled() {
        return CONFIG.recipeBook != null && CONFIG.recipeBook.hideButton;
    }

    public static float overlayTextScale() {
        float s = (CONFIG.overlay != null) ? CONFIG.overlay.textScale : 1.0f;
        if (Float.isNaN(s) || s <= 0.05f) s = 1.0f;
        return Math.min(s, 10.0f);
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

    public static int coordsColorARGB() { return parseLeadingColor((CONFIG.overlay != null) ? CONFIG.overlay.coordsFormat : null).argb; }
    public static int timeColorARGB()   { return parseLeadingColor((CONFIG.overlay != null) ? CONFIG.overlay.timeDayFormat : null).argb; }

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

    public static String ticksToHHMM(long dayTime) {
        long ticks = ((dayTime % 24000L) + 24000L) % 24000L;
        long adj = (ticks + 6000L) % 24000L;
        int hours = (int) (adj / 1000L);
        int minutes = (int) ((adj % 1000L) * 60L / 1000L);
        return String.format("%02d:%02d", hours, minutes);
    }

    private static Parsed parseLeadingColor(String s) {
        if (s != null && s.length() >= 2 && (s.charAt(0) == '&' || s.charAt(0) == '§')) {
            Integer rgb = mcColorCodeToRGB(Character.toLowerCase(s.charAt(1)));
            if (rgb != null) return new Parsed(s.substring(2), 0xFF000000 | rgb);
        }
        return new Parsed(s == null ? "" : s, 0xFFFFFFFF);
    }
    private static String cycleLeadingColor(String fmt) {
        char[] order = new char[]{'f','6','b','a','c','e','9','d','2','3','4','5','7','8','0'};
        char current = 'f';
        if (fmt != null && fmt.length() >= 2 && (fmt.charAt(0) == '&' || fmt.charAt(0) == '§'))
            current = Character.toLowerCase(fmt.charAt(1));
        int idx = 0;
        for (int i = 0; i < order.length; i++) if (order[i] == current) { idx = (i + 1) % order.length; break; }
        String tail = (fmt == null) ? "" : ((fmt.length() >= 2 && (fmt.charAt(0) == '&' || fmt.charAt(0) == '§')) ? fmt.substring(2) : fmt);
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

    public static boolean locatorBarEnabled() {
        return CONFIG.locatorBar != null && CONFIG.locatorBar.enabled;
    }
    public static boolean locatorBarShowInSpectator() {
        return CONFIG.locatorBar != null && CONFIG.locatorBar.showInSpectator;
    }
    public static boolean tabForcesLocatorBar() {
        return CONFIG.locatorBar == null || CONFIG.locatorBar.tabForcesLocatorBar;
    }
    public static boolean tabShowsNames() {
        return CONFIG.locatorBar == null || CONFIG.locatorBar.tabShowsNames;
    }
    public static int locatorHideDelayMs() {
        return (CONFIG.locatorBar != null) ? Math.max(0, CONFIG.locatorBar.hideDelayMs) : 800;
    }
    public static boolean showRecovery() { return CONFIG.locatorBar == null || CONFIG.locatorBar.showRecovery; }
    public static int recoveryColor()    { return CONFIG.locatorBar != null ? CONFIG.locatorBar.recoveryColor : 0xFFFF5555; }
    public static boolean showLodestone() { return CONFIG.locatorBar == null || CONFIG.locatorBar.showLodestone; }
    public static int lodestoneColor()    { return CONFIG.locatorBar != null ? CONFIG.locatorBar.lodestoneColor : 0xFF55AAFF; }
    public static boolean scanInventories() { return CONFIG.locatorBar == null || CONFIG.locatorBar.ScanInventories; }
    public static boolean allowNameColorCodes() { return CONFIG.locatorBar == null || CONFIG.locatorBar.allowNameColorCodes; }
    public static boolean renderPlayerHeads() { return CONFIG.locatorBar == null || CONFIG.locatorBar.renderPlayerHeads; }
    public static boolean coloredHeadOutline() { return CONFIG.locatorBar == null || CONFIG.locatorBar.coloredHeadOutline; }
    public static float headSizeMultiplier() {
        float f = (CONFIG.locatorBar != null) ? CONFIG.locatorBar.headSizeMultiplier : 1.0f;
        if (Float.isNaN(f) || f <= 0.25f) f = 1.0f;
        return Math.min(f, 4.0f);
    }

    public static void setLocatorBarEnabled(boolean enabled) {
        if (CONFIG.locatorBar == null) CONFIG.locatorBar = new ClientConfig.LocatorBar();
        CONFIG.locatorBar.enabled = enabled;
        save();
    }
    public static void toggleLocatorBar() { setLocatorBarEnabled(!locatorBarEnabled()); }

    public static boolean f3NeedsCompass() {
        return CONFIG.f3 == null || CONFIG.f3.f3needscompass;
    }
    public static boolean f3NeedsClock() {
        return CONFIG.f3 == null || CONFIG.f3.f3needsclock;
    }

    public static boolean allowRealmsButtons() {
        return CONFIG.menu == null || CONFIG.menu.allowRealmsButtons;
    }

    public static boolean allowAccessibilityButton() {
        return CONFIG.menu == null || CONFIG.menu.allowAccessibilityButton;
    }

    public static boolean copyScreenshotsToClipboard() {
        return CONFIG.menu == null || CONFIG.menu.copyScreenshotsToClipboard;
    }


    public static void saveClient() { save(); }
}
