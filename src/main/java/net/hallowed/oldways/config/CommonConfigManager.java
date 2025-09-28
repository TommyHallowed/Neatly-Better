package net.hallowed.oldways.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.hallowed.TheOldWays;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Common config loader with "merge defaults + round-trip save".
 * - Loads existing JSON (if present).
 * - Deep-merges it over a fresh defaults instance so user values win, missing keys keep defaults.
 * - Saves back only when the on-disk JSON would change (adds new keys without wiping user edits).
 * - Exposes reload() to refresh at runtime.
 */
public final class CommonConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve(TheOldWays.MOD_ID + ".common.json");

    private static CommonConfig CONFIG = new CommonConfig();

    private CommonConfigManager() {}

    /** Load from disk, deep-merge with defaults for new/missing fields, and write back if upgraded. */
    public static void load() {
        try {
            Files.createDirectories(FILE.getParent());

            String beforeJson = null;
            CommonConfig loaded = null;

            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                    loaded = GSON.fromJson(r, CommonConfig.class);
                } catch (Exception e) {
                    TheOldWays.LOGGER.error("[{}] Invalid JSON in {}, falling back to defaults (will rewrite): {}",
                            TheOldWays.MOD_ID, FILE.getFileName(), e.toString());
                }
                if (Files.exists(FILE)) {
                    // Keep original for change detection
                    beforeJson = Files.readString(FILE, StandardCharsets.UTF_8);
                }
            }

            // Build fresh defaults and deep-merge user values over them.
            CommonConfig defaults = new CommonConfig();
            CommonConfig merged = mergeWithDefaults(defaults, loaded);
            CONFIG = merged;

            String afterJson = GSON.toJson(merged);

            // Write if file doesn't exist or structure/content changed (new keys, etc.).
            if (!Files.exists(FILE) || !Objects.equals(normalize(beforeJson), normalize(afterJson))) {
                save();
            }
        } catch (IOException e) {
            TheOldWays.LOGGER.error("[{}] Failed to load common config: {}", TheOldWays.MOD_ID, e.toString());
        }
    }

    /** Deep-merge: user's values override defaults; missing keys keep defaults. */
    private static CommonConfig mergeWithDefaults(CommonConfig defaults, CommonConfig incoming) {
        if (incoming == null) return defaults;

        JsonObject defObj = GSON.toJsonTree(defaults).getAsJsonObject();
        JsonObject inObj  = GSON.toJsonTree(incoming).getAsJsonObject();

        deepMergeObjects(inObj, defObj); // copy user values into defaults (recursively)
        return GSON.fromJson(defObj, CommonConfig.class);
    }

    /** Recursively copies keys from src into dst; objects merge, other types overwrite. */
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
                // primitives/arrays/null → user's value wins
                dst.add(key, srcVal);
            }
        }
    }

    /** Normalize for comparison (null-safe, EOL-insensitive). */
    private static String normalize(String json) {
        return json == null ? null : json.replace("\r\n", "\n").trim();
    }

    /** Persist current CONFIG to disk. */
    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(CONFIG, w);
        } catch (IOException e) {
            TheOldWays.LOGGER.error("[{}] Failed to save common config: {}", TheOldWays.MOD_ID, e.toString());
        }
    }

    /** Hot reload (e.g., from a command or keybind). */
    public static void reload() {
        load();
    }

    /** Accessor for other systems. */
    public static CommonConfig get() {
        return CONFIG;
    }

    // === Convenience getters (adapt/trim to match your CommonConfig) ===
    public static boolean elytraBoostingEnabled() { return CONFIG.elytraBoosting.enabled; }
    public static boolean infinityFixEnabled()    { return CONFIG.infinityFix.enabled; }
    public static boolean bedNerfEnabled()        { return CONFIG.bedNerf.enabled; }
    public static boolean mendingNerfEnabled()    { return CONFIG.mendingNerf.enabled; }
    public static boolean protectionNerfEnabled() { return CONFIG.protectionNerf.enabled; }
    public static boolean velocityFix()           { return CONFIG.velocityFix.enabled; }
    public static boolean oldEnchant()            { return CONFIG.oldEnchant.enabled; }

    public static boolean villagerGlobalCuringPrices()      { return CONFIG.villager.globalCuringPrices; }
    public static boolean villagerInfiniteCuringDiscounts() { return CONFIG.villager.infiniteCuringDiscounts; }

    public static int totemCooldownSeconds() { return Math.max(0, CONFIG.totemCooldown.seconds); }
    public static int totemCooldownTicks()   { return totemCooldownSeconds() * 20; }
}
