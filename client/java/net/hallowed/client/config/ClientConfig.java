package net.hallowed.client.config;

import com.google.gson.annotations.SerializedName;

/** Top-level client config. */
public final class ClientConfig {

    @SerializedName("Old Item Rendering")
    public OldItemRendering oldItemRendering = new OldItemRendering();

    @SerializedName("Overlay")
    public Overlay overlay = new Overlay();


    /** Client HUD overlay options (time/day + coordinates). */
    public static final class Overlay {
        /** Show the small corner overlay when F3 is closed. */
        public boolean enabled = true;

        /** Independently show/hide each line (toggled from the inventory icons). */
        public boolean coordsVisible = true;
        public boolean timeVisible   = true;

        /** Position preset: "top_left", "top_right", "bottom_left", "bottom_right" */
        public String position = "top_left";

        /** Text scale multiplier. 1.0 = vanilla size. */
        public float textScale = 1.0f;

        /** Leading color codes supported directly in formats (e.g. "&6..."). */
        public String timeDayFormat = "time: {time} | Day: {day}";
        public String coordsFormat  = "x: {x} | y: {y} | z: {z}";
    }
    //old Item Rendering
    public static final class OldItemRendering { public boolean enabled = true; }
}
