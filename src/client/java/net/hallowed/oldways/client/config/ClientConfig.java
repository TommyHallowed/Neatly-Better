package net.hallowed.oldways.client.config;

import com.google.gson.annotations.SerializedName;


public final class ClientConfig {

    @SerializedName("Old Features")
    public OldFeatures oldFeatures = new OldFeatures();

    @SerializedName("Overlay")
    public Overlay overlay = new Overlay();

    @SerializedName("Recipe Book")
    public RecipeBook recipeBook = new RecipeBook();

    @SerializedName("Stuck Projectiles")
    public StuckProjectiles stuckProjectiles = new StuckProjectiles();

    @SerializedName("Locator Bar")
    public LocatorBar locatorBar = new LocatorBar();

    @SerializedName("F3")
    public F3 f3 = new F3();

    @SerializedName("UI Tweaks")
    public menu menu = new menu();

    public static final class OldFeatures {
        @SerializedName("_comment")
        public String comment = "Old/removed client-sided features.";
        public boolean oldItemRendering = false;
        public boolean potionGlint = false;
    }

    public static final class Overlay {
        @SerializedName("_comment")
        public String comment = "Compass/Clock Overlay";
        public boolean enabled = true;
        public boolean coordsVisible = true;
        public boolean timeVisible = true;
        public String position = "top_left";
        public String coordsPosition = "top_left";
        public String timePosition = "top_left";
        public float textScale = 1.0f;
        public String timeDayFormat = "Day {day} | {time}";
        public String coordsFormat = "x: {x} | y: {y} | z: {z}";
    }

    public static final class RecipeBook {
        @SerializedName("_comment")
        public String comment = "Recipe Book tweaks";
        public boolean autoClose = true;
        public boolean hideButton = false;
    }

    public static final class StuckProjectiles {
        @SerializedName("_comment")
        public String comment = "Show stuck arrows/bee stingers on all mobs";
        public boolean enabled = true;
    }

    public static final class LocatorBar {
        @SerializedName("_comment")
        public String comment = "Locator bar tweaks and client waypoints (lodestones / recovery compass)";
        public boolean enabled = true;
        public boolean showInSpectator = false;
        public boolean tabForcesLocatorBar = true;
        public boolean tabShowsNames = true;
        public int hideDelayMs = 800;
        public boolean showRecovery = true;
        public int recoveryColor = 0xFFFF5555;
        public boolean showLodestone = true;
        public int lodestoneColor = 0xFF55AAFF;
        public boolean ScanInventories = true;
        public boolean allowNameColorCodes = true;
        public boolean renderPlayerHeads = true;
        public boolean coloredHeadOutline = true;
        public float headSizeMultiplier = 1.0f;
    }

    public static final class F3 {
        @SerializedName("_comment")
        public String comment = "Need for compass/clock in inventory/ender chest to show f3 info";
        public boolean f3needscompass = true;
        public boolean f3needsclock = true;
    }

    public static final class menu {
        public boolean allowRealmsButtons = true;
        public boolean allowAccessibilityButton = true;
        public boolean copyScreenshotsToClipboard = true;
    }
}
