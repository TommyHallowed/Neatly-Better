package net.hallowed.oldways.client.config;

import com.google.gson.annotations.SerializedName;

/** Client-only config. Comments are plain text written directly into the JSON. */
public final class ClientConfig {

    // === NEW: Old Features category (unifies old item rendering + potion glint) ===
    @SerializedName("Old Features")
    public OldFeatures oldFeatures = new OldFeatures();

    @SerializedName("Overlay")
    public Overlay overlay = new Overlay();

    @SerializedName("Recipe Book")
    public RecipeBook recipeBook = new RecipeBook();

    @SerializedName("Stuck Projectiles")
    public StuckProjectiles stuckProjectiles = new StuckProjectiles();

    /* ========================= Locator Bar ========================= */
    @SerializedName("Locator Bar")
    public LocatorBar locatorBar = new LocatorBar();

    // ----------------------------------------------------------------
    // Sections
    // ----------------------------------------------------------------

    /** NEW unified bucket for classic client visuals. */
    public static final class OldFeatures {
        @SerializedName("_comment")
        public String comment = "Old/removed client-sided features.";

        /** Old (beta-like) ground item rendering */
        public boolean oldItemRendering = false;

        /** Potions with effects render with the enchantment glint */
        public boolean potionGlint = false;
    }

    /* ========================= Small HUD overlay ========================= */
    public static final class Overlay {
        @SerializedName("_comment")
        public String comment = "Compass/Clock Overlay";

        /** Master toggle */
        public boolean enabled = true;

        /** Per-line visibility */
        public boolean coordsVisible = true;
        public boolean timeVisible   = true;

        /** Legacy block position (fallback for both) */
        public String position       = "top_left"; // top_left, top_right, bottom_left, bottom_right

        /** Independent positions */
        public String coordsPosition = "top_left";
        public String timePosition   = "top_left";

        /** Text scale multiplier (1.0 = vanilla size) */
        public float textScale       = 1.0f;

        /** Supports {time} and {day} with optional leading &x/§x color */
        public String timeDayFormat  = "Day {day} | {time}";

        /** Supports {x},{y},{z} with optional leading &x/§x color */
        public String coordsFormat   = "x: {x} | y: {y} | z: {z}";
    }

    /* ========================= Recipe book (client-side UX) ========================= */
    public static final class RecipeBook {
        @SerializedName("_comment")
        public String comment = "Recipe Book tweaks";

        /** Close the book automatically when the screen closes (your existing feature). */
        public boolean autoClose = true;

        /** Hide the recipe-book toggle button in the Survival Inventory. */
        public boolean hideButton = false;
    }

    /* ========================= Stuck projectiles ========================= */
    public static final class StuckProjectiles {
        @SerializedName("_comment")
        public String comment = "Show stuck arrows/bee stingers on all mobs";
        public boolean enabled = true; // default ON
    }

    /* ========================= Locator Bar ========================= */
    public static final class LocatorBar {
        @SerializedName("_comment")
        public String comment = "Locator bar tweaks and client waypoints (lodestones / recovery compass)";

        /** Master toggle – disables ALL client-side waypoint logic and the extra rendering. */
        public boolean enabled = true;

        /** If true, show the locator bar even when spectating. */
        public boolean showInSpectator = false;

        /** Holding TAB (player list) forces the vanilla locator bar to show. */
        public boolean tabForcesLocatorBar = true;

        /** While TAB is held, show the closest client waypoint’s name above the bar. */
        public boolean tabShowsNames = true;

        /** How long (ms) to keep the bar visible after the last matching waypoint disappears. */
        public int hideDelayMs = 800;

        /* -------- Sources to scan for client waypoints -------- */
        /** Track Recovery Compass (deathpoint) in the current dimension. */
        public boolean showRecovery = true;

        /** ARGB color used for recovery waypoint if the item name has no #RRGGBB. */
        public int recoveryColor = 0xFFFF5555;

        /** Track Lodestone compasses in the current dimension. */
        public boolean showLodestone = true;

        /** ARGB fallback color for lodestone waypoint icons. */
        public int lodestoneColor = 0xFF55AAFF;

        /** If true, scan items inside bundles as well. */
        public boolean ScanInventories = true;

        /** Allow overriding colors by placing a #RRGGBB code in the item’s custom name. */
        public boolean allowNameColorCodes = true;

        /* -------- Optional “player heads” overlay on icons -------- */
        /** Try to render a player head if a waypoint’s text equals a visible player’s name. */
        public boolean renderPlayerHeads = true;

        /** Draw a colored 1px outline around heads (uses the waypoint color). */
        public boolean coloredHeadOutline = true;

        /** Scale factor for rendered heads (1.0 = 9×9). */
        public float headSizeMultiplier = 1.0f;
    }
}
