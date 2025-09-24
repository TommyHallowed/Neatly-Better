package net.hallowed.oldways.client.config;

import com.google.gson.annotations.SerializedName;

/** Client-only config. Comments are plain text written directly into the JSON. */
public final class ClientConfig {

    @SerializedName("Old Item Rendering")
    public OldItemRendering oldItemRendering = new OldItemRendering();

    @SerializedName("Overlay")
    public Overlay overlay = new Overlay();

    @SerializedName("Recipe Book")
    public RecipeBook recipeBook = new RecipeBook();

    @SerializedName("Stuck Projectiles")
    public StuckProjectiles stuckProjectiles = new StuckProjectiles();

    /** Old (2D-like) item rendering toggle */
    public static final class OldItemRendering {
        @SerializedName("_comment")
        public String comment = "Restores the old beta item rendering";
        public boolean enabled = true;
    }

    /** Small HUD overlay with time/day and coordinates */
    public static final class Overlay {
        @SerializedName("_comment")
        public String comment = "Compass/Clock Overlay";

        /** Master toggle */
        public boolean enabled = true;

        /** Per-line visibility */
        public boolean coordsVisible = true;
        public boolean timeVisible = true;

        /** Legacy block position (fallback) */
        public String position = "top_left"; // top_left, top_right, bottom_left, bottom_right

        /** Independent positions */
        public String coordsPosition = "top_left";
        public String timePosition   = "top_left";

        /** Text scale multiplier (1.0 = vanilla size) */
        public float textScale = 1.0f;

        /** Supports {time} and {day} with optional leading &x/§x color */
        public String timeDayFormat = "time: {time} | Day: {day}";

        /** Supports {x},{y},{z} with optional leading &x/§x color */
        public String coordsFormat  = "x: {x} | y: {y} | z: {z}";
    }

    /** Recipe book UX tweaks (client-side only) */
    public static final class RecipeBook {
        @SerializedName("_comment")
        public String comment = "Auto close recipe book";
        public boolean autoClose = true;
    }

    /** Toggle stuck arrow/stinger rendering for all mobs (client-side only) */
    public static final class StuckProjectiles {
        @SerializedName("_comment")
        public String comment = "Show stuck arrows/bee stingers on all mobs";
        public boolean enabled = true; // default ON
    }
}
