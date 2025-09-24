package net.hallowed.oldways.client.ui;

import net.minecraft.util.Identifier;

public final class GuiSprites {
    private static Identifier id(String path) {
        return Identifier.of("old-ways", path);
    }

    // icons (shown/hidden)
    public static final Identifier CLOCK_SHOWN      = id("gui/sprites/clock_icon_shown");
    public static final Identifier CLOCK_HIDDEN     = id("gui/sprites/clock_icon_hidden");
    public static final Identifier COMPASS_SHOWN    = id("gui/sprites/compass_icon_shown");
    public static final Identifier COMPASS_HIDDEN   = id("gui/sprites/compass_icon_hidden");

    // outlines (tinted)
    public static final Identifier CLOCK_OUTLINE    = id("gui/sprites/clock_icon_outline");
    public static final Identifier COMPASS_OUTLINE  = id("gui/sprites/compass_icon_outline");

    private GuiSprites() {}
}
