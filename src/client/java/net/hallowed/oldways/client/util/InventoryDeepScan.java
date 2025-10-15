package net.hallowed.oldways.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class InventoryDeepScan {
    private InventoryDeepScan() {}

    private static final long TTL_NS = 200_000_000L; // 200ms

    private static long lastScanNs = 0L;
    private static boolean hasCompassCached = false;
    private static boolean hasRecoveryCompassCached = false;
    private static boolean hasClockCached   = false;

    public static boolean hasCompass(PlayerEntity p) {
        refreshIfNeeded(p); return hasCompassCached;
    }

    public static boolean hasAnyCompass(PlayerEntity p) {
        refreshIfNeeded(p); return hasCompassCached || hasRecoveryCompassCached;
    }
    public static boolean hasClock(PlayerEntity p) {
        refreshIfNeeded(p); return hasClockCached;
    }

    @SuppressWarnings("unused")
    public static void invalidate() { lastScanNs = 0L; }

    private static void refreshIfNeeded(PlayerEntity p) {
        if (p == null) {
            var mc = MinecraftClient.getInstance();
            p = (mc != null) ? mc.player : null;
        }
        if (p == null) return;

        long now = System.nanoTime();
        if ((now - lastScanNs) <= TTL_NS) return;
        lastScanNs = now;

        hasCompassCached         = hasItemDeep(p, Items.COMPASS);
        hasRecoveryCompassCached = hasItemDeep(p, Items.RECOVERY_COMPASS);
        hasClockCached           = hasItemDeep(p, Items.CLOCK);
    }

    private static boolean hasItemDeep(PlayerEntity p, Item target) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) if (matchesDeep(inv.getStack(i), target)) return true;
        return matchesDeep(p.getOffHandStack(), target);
    }

    private static boolean matchesDeep(ItemStack stack, Item target) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.isOf(target)) return true;

        BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (ItemStack child : bundle.iterate()) {
                if (!child.isEmpty() && matchesDeep(child, target)) return true;
            }
        }
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            for (ItemStack child : container.iterateNonEmpty()) {
                if (matchesDeep(child, target)) return true;
            }
        }
        return false;
    }
}
