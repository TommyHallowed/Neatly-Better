package net.hallowed.oldways.client.util;

import net.minecraft.util.DyeColor;

public final class RainbowCycle {
    private RainbowCycle() {}

    /** 25-tick crossfade between the 16 DyeColors, like jeb_ sheep. */
    public static int argbFromAge(float ageTicks, int seed) {
        final int period = 25;
        final DyeColor[] C = DyeColor.values();
        int base = (int)Math.floor(ageTicks / period);
        int i = floorMod(base + seed, C.length);
        int j = (i + 1) % C.length;
        float t = (ageTicks % period) / (float)period;

        int c1 = C[i].getEntityColor();
        int c2 = C[j].getEntityColor();

        int r = (int)( ((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t + 0.5f );
        int g = (int)( ((c1 >> 8)  & 0xFF) + (((c2 >> 8)  & 0xFF) - ((c1 >> 8)  & 0xFF)) * t + 0.5f );
        int b = (int)( ( c1        & 0xFF) + (( c2        & 0xFF) - ( c1        & 0xFF)) * t + 0.5f );
        return 0xFF_000000 | (r << 16) | (g << 8) | b;
    }

    private static int floorMod(int x, int m) { int r = x % m; return r < 0 ? r + m : r; }
}
