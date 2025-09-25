package net.hallowed.oldways.client.rainbow;

import net.minecraft.util.math.MathHelper;

/**
 * Computes the rainbow color from the *client animation tick* (same clock .mcmeta uses).
 * Use computeRgb() for item tint and computeArgb() for model-part tint in renderers.
 */
public final class RainbowTicker {
    private RainbowTicker() {}

    // Vanilla-ish RGB table (0..1) in DyeColor order
    private static final float[][] DYE_RGB = new float[][]{
            {1.00f, 1.00f, 1.00f}, {0.85f, 0.50f, 0.20f}, {0.70f, 0.30f, 0.85f}, {0.40f, 0.60f, 0.85f},
            {0.90f, 0.90f, 0.20f}, {0.50f, 0.80f, 0.10f}, {0.95f, 0.50f, 0.65f}, {0.30f, 0.30f, 0.35f},
            {0.60f, 0.60f, 0.70f}, {0.30f, 0.50f, 0.60f}, {0.70f, 0.40f, 0.90f}, {0.25f, 0.30f, 0.60f},
            {0.40f, 0.30f, 0.15f}, {0.40f, 0.50f, 0.20f}, {0.70f, 0.25f, 0.25f}, {0.10f, 0.10f, 0.10f}
    };

    /** frametime in your .mcmeta */
    private static final int PERIOD = 25;

    /** RGB (0xRRGGBB) for item color providers. */
    public static int computeRgb() {
        int argb = computeArgb();
        return argb & 0x00FFFFFF;
    }

    /** ARGB (0xAARRGGBB) for model-part tint in renderers. */
    public static int computeArgb() {
        long t = getClientAnimationTicks();
        int steps = DYE_RGB.length;

        int idx  = Math.floorMod((int)(t / PERIOD), steps);
        int next = (idx + 1) % steps;
        float mix = (t % PERIOD) / (float) PERIOD;

        float[] A = DYE_RGB[idx];
        float[] B = DYE_RGB[next];

        float r = A[0] * (1f - mix) + B[0] * mix;
        float g = A[1] * (1f - mix) + B[1] * mix;
        float b = A[2] * (1f - mix) + B[2] * mix;

        int ir = MathHelper.clamp((int)(r * 255f), 0, 255);
        int ig = MathHelper.clamp((int)(g * 255f), 0, 255);
        int ib = MathHelper.clamp((int)(b * 255f), 0, 255);
        return 0xFF000000 | (ir << 16) | (ig << 8) | ib;
    }

    /**
     * Mapping-safe access to the *client animation tick* (the texture atlas clock).
     * Tries several common names across 1.21.x mappings; falls back to world time if needed.
     */
    public static long getClientAnimationTicks() {
        try {
            Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
            Object mc = mcClass.getMethod("getInstance").invoke(null);

            Object rtc;
            try {
                rtc = mcClass.getMethod("getRenderTickCounter").invoke(mc);
            } catch (NoSuchMethodException e) {
                var f = mcClass.getDeclaredField("renderTickCounter");
                f.setAccessible(true);
                rtc = f.get(mc);
            }

            if (rtc != null) {
                Class<?> rtcClass = rtc.getClass();
                // Try methods first
                for (String m : new String[]{"getTicks", "getTickCount"}) {
                    try {
                        Object v = rtcClass.getMethod(m).invoke(rtc);
                        return ((Number) v).longValue();
                    } catch (NoSuchMethodException ignored) {}
                }
                // Then fields
                for (String f : new String[]{"ticks", "tickCount"}) {
                    try {
                        var fld = rtcClass.getDeclaredField(f);
                        fld.setAccessible(true);
                        Object v = fld.get(rtc);
                        return ((Number) v).longValue();
                    } catch (NoSuchFieldException ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        // Fallback: world time (can desync if game paused, but better than 0)
        try {
            Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
            Object mc = mcClass.getMethod("getInstance").invoke(null);
            var worldF = mcClass.getDeclaredField("world");
            worldF.setAccessible(true);
            Object world = worldF.get(mc);
            if (world != null) {
                Object v = world.getClass().getMethod("getTime").invoke(world);
                return ((Number) v).longValue();
            }
        } catch (Throwable ignored2) {}

        return 0L;
    }
}
