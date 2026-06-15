package net.hallowed.neatlybetter.client.util;

import net.minecraft.resources.Identifier;

public class ModTextures {

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("neatly-better", path);
    }

    // ---- GUI textures ----
    public static final Identifier RECRAFT_BUTTON  = id("textures/gui/recraft_button.png");
    public static final Identifier COMPASS_SHOWN   = id("textures/gui/overlay/compass_icon_shown.png");
    public static final Identifier COMPASS_HIDDEN  = id("textures/gui/overlay/compass_icon_hidden.png");
    public static final Identifier CLOCK_SHOWN     = id("textures/gui/overlay/clock_icon_shown.png");
    public static final Identifier CLOCK_HIDDEN    = id("textures/gui/overlay/clock_icon_hidden.png");

    public static final Identifier SHULKER_TOOLTIP    = id("shulker_tooltip");
}