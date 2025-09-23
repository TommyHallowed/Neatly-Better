package net.hallowed.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.hallowed.TheOldWays;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CommonConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve(TheOldWays.MOD_ID + ".common.json");

    private static CommonConfig CONFIG = new CommonConfig();

    private CommonConfigManager() {}

    public static void load() {
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                    CommonConfig loaded = GSON.fromJson(r, CommonConfig.class);
                    if (loaded != null) CONFIG = loaded;
                }
            } else {
                save(); // write defaults
            }
        } catch (IOException e) {
            TheOldWays.LOGGER.error("[{}] Failed to load common config: {}", TheOldWays.MOD_ID, e.toString());
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(CONFIG, w);
        } catch (IOException e) {
            TheOldWays.LOGGER.error("[{}] Failed to save common config: {}", TheOldWays.MOD_ID, e.toString());
        }
    }

    public static CommonConfig get() { return CONFIG; }

    // Convenience
    public static boolean mendingNerfEnabled() { return CONFIG.mendingNerf.enabled; }

    public static boolean elytraBoostingEnabled() { return CONFIG.elytraBoosting.enabled; }

    public static boolean infinityFixEnabled() { return CONFIG.infinityFix.enabled; }

    public static boolean bedNerfEnabled() { return CONFIG.bedNerf.enabled; }


    public static int totemCooldownSeconds() {
        int s = CONFIG.totemCooldown.seconds;
        return Math.max(0, s);
    }

    public static int totemCooldownTicks() {
        return totemCooldownSeconds() * 20;
    }
}
