package net.hallowed.oldways.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

public final class InventoryDeepScan {
    private InventoryDeepScan() {}

    private static final long TTL_NS = 200_000_000L; // 200ms

    private static long lastScanNs = 0L;
    private static boolean hasCompassCached = false;
    private static boolean hasRecoveryCompassCached = false;
    private static boolean hasClockCached   = false;

    public static boolean hasCompass(Player p) {
        refreshIfNeeded(p); return hasCompassCached;
    }

    public static boolean hasAnyCompass(Player p) {
        refreshIfNeeded(p); return hasCompassCached || hasRecoveryCompassCached;
    }
    public static boolean hasClock(Player p) {
        refreshIfNeeded(p); return hasClockCached;
    }

    @SuppressWarnings("unused")
    public static void invalidate() { lastScanNs = 0L; }

    private static void refreshIfNeeded(Player p) {
        if (p == null) {
            var mc = Minecraft.getInstance();
            p = mc.player;
        }
        if (p == null) return;

        long now = System.nanoTime();
        if ((now - lastScanNs) <= TTL_NS) return;
        lastScanNs = now;

        hasCompassCached         = hasItemDeep(p, Items.COMPASS);
        hasRecoveryCompassCached = hasItemDeep(p, Items.RECOVERY_COMPASS);
        hasClockCached           = hasItemDeep(p, Items.CLOCK);
    }

    private static boolean hasItemDeep(Player p, Item target) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) if (matchesDeep(inv.getItem(i), target)) return true;
        return matchesDeep(p.getOffhandItem(), target);
    }

    private static boolean matchesDeep(ItemStack stack, Item target) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(target)) return true;

        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (ItemStack child : bundle.items()) {
                if (!child.isEmpty() && matchesDeep(child, target)) return true;
            }
        }
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            for (ItemStack child : container.nonEmptyItems()) {
                if (matchesDeep(child, target)) return true;
            }
        }
        return false;
    }
}
