package net.hallowed.oldways.client.util;

/** Small, thread-local render state so swaps are scoped to the exact draw we want. */
public final class BannerSwapState {
    private BannerSwapState() {}

    /** True only while a rainbow *block entity* is rendering (world). */
    public static final ThreadLocal<Boolean> OUR_BLOCK = ThreadLocal.withInitial(() -> false);

    /** True only while a rainbow *item icon* is rendering (inventory/GUI/hand). */
    public static final ThreadLocal<Boolean> OUR_ITEM  = ThreadLocal.withInitial(() -> false);

    /** Set during getData(..) and consumed at render(..) HEAD to avoid leaking to other draws. */
    public static final ThreadLocal<Boolean> PENDING_ITEM = ThreadLocal.withInitial(() -> false);

    public static void clearAll() {
        OUR_BLOCK.remove();
        OUR_ITEM.remove();
        PENDING_ITEM.remove();
    }
}
