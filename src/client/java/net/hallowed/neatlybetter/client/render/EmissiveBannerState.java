package net.hallowed.neatlybetter.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class EmissiveBannerState {
    private static boolean emissive;

    private EmissiveBannerState() {}

    public static void setEmissive(boolean value) { emissive = value; }
    public static boolean isEmissive() { return emissive; }
}
