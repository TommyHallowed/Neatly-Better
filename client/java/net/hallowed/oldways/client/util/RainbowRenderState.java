package net.hallowed.oldways.client.util;

public final class RainbowRenderState {
    private RainbowRenderState() {}
    public static final ThreadLocal<Boolean> ENABLED = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<Integer>  ARGB    = ThreadLocal.withInitial(() -> 0xFFFFFFFF);
    public static final ThreadLocal<Integer>  SEED    = ThreadLocal.withInitial(() -> 0);
    public static void begin(boolean on, int argb, int seed){ ENABLED.set(on); ARGB.set(argb); SEED.set(seed); }
    public static void end(){ ENABLED.remove(); ARGB.remove(); SEED.remove(); }
}
