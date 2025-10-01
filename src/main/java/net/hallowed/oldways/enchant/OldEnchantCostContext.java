package net.hallowed.oldways.enchant;

public final class OldEnchantCostContext {
    private static final ThreadLocal<Integer> REQUIRED = new ThreadLocal<>();
    private static final ThreadLocal<Integer> START_LVL = new ThreadLocal<>();
    private OldEnchantCostContext() {}
    public static void push(int required, int startLevel) { REQUIRED.set(required); START_LVL.set(startLevel); }
    public static Integer peekRequired() { return REQUIRED.get(); }
    public static Integer peekStartLevel() { return START_LVL.get(); }
    public static void clear() { REQUIRED.remove(); START_LVL.remove(); }
}
