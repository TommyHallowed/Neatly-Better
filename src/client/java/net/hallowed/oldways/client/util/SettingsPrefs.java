package net.hallowed.oldways.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SettingsPrefs {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance()
            .getGameDir().resolve("data/oldWaysSettings.dat");

    /** ===== Gameplay screen ===== */
    public boolean tabShowsNames = true;
    public RecipeBookMode recipeBookMode = RecipeBookMode.AUTOHIDE;
    public boolean showStuckProjectiles = true;
    public boolean showPotionGlint = true;
    public boolean render2DItems = false;
    public boolean realmsButtons = false;
    public boolean accessibilityButton = false;
    public boolean copyScreenshots = true;
    public boolean telemetryOff = true;

    /** ===== Overlay screen ===== */
    public boolean showCoords = true;
    public boolean showTime = true;
    public Corner coordsPos = Corner.TOP_LEFT;
    public Corner timePos = Corner.TOP_LEFT;
    public String coordsFormat  = "XYZ: {x} | {y} | {z}";
    public String timeDayFormat = "Day: {day} | {time}";

    public enum RecipeBookMode {
        AUTOHIDE("options.gameplay.recipe_book.autohide"),
        HIDDEN("options.gameplay.recipe_book.hidden"),
        SHOWN("options.gameplay.recipe_book.shown");

        public final String langKey;
        RecipeBookMode(String k) { this.langKey = k; }
        public RecipeBookMode next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public enum Corner {
        TOP_LEFT("options.gameplay.overlay.pos.top_left"),
        TOP_RIGHT("options.gameplay.overlay.pos.top_right"),
        BOTTOM_RIGHT("options.gameplay.overlay.pos.bottom_right"),
        BOTTOM_LEFT("options.gameplay.overlay.pos.bottom_left");

        public final String langKey;
        Corner(String k) { this.langKey = k; }
        public Corner next() { return values()[(ordinal() + 1) % values().length]; }
    }

    private static SettingsPrefs INSTANCE;

    private SettingsPrefs() {}

    public static SettingsPrefs get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(get(), w);
            }
        } catch (IOException ignored) { }
    }

    private static SettingsPrefs load() {
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                SettingsPrefs p = GSON.fromJson(r, SettingsPrefs.class);
                if (p != null) return p;
            } catch (IOException ignored) { }
        }
        return new SettingsPrefs();
    }
}
