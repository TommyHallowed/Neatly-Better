package net.hallowed.oldways.enchant;

public final class OldEnchantCostContext {
    private static final ThreadLocal<Integer> COST = new ThreadLocal<>();
    private OldEnchantCostContext() {}

    public static void push(int cost) { COST.set(cost); }
    public static Integer peek()      { return COST.get(); }
    public static void clear()        { COST.remove(); }
}
