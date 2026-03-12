package net.hallowed.oldways.client.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Soft-dependency helper for MrCrayfish's Backpacked mod.
 * Allows item scanning (compass, clock, waypoints) to find items stored in backpacks.
 *
 * <p>Works by returning the equipped backpack {@link ItemStack}s, each of which carries
 * {@code DataComponents.CONTAINER} with its contents. The existing deep-scan logic in
 * {@link net.hallowed.oldways.client.util.InventoryDeepScan} and {@link net.hallowed.oldways.client.feature.locator.WaypointTracking}
 * already handles that component recursively — so no extra scanning code is needed.</p>
 */
public final class BackpackedCompat {
    private BackpackedCompat() {}

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("backpacked");

    /**
     * Returns all equipped backpack ItemStacks for the given player.
     * Each stack carries {@code DataComponents.CONTAINER} with its contents.
     *
     * @return backpack stacks, or empty list if Backpacked is absent
     */
    public static List<ItemStack> getBackpackStacks(Player player) {
        if (!LOADED || player == null) return Collections.emptyList();
        try {
            return Accessor.getStacks(player);
        } catch (Throwable t) {
            // Class loading failure, version mismatch, etc. — fail silently
            return Collections.emptyList();
        }
    }

    /**
     * Isolated inner class that references Backpacked types.
     * Only loaded by the JVM when {@link #LOADED} is true, preventing
     * {@code ClassNotFoundException} when Backpacked is not installed.
     */
    private static final class Accessor {
        static List<ItemStack> getStacks(Player player) {
            return com.mrcrayfish.backpacked.BackpackHelper.getBackpacks(player);
        }
    }
}
