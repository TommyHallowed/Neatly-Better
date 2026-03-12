package net.hallowed.oldways.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Server-side Backpacked compat.  Uses the same class-isolation pattern as the
 * client-side {@code BackpackedCompat}: an inner {@code Accessor} class that is
 * only loaded when Backpacked is present, guarded by a static {@code isModLoaded}
 * check.
 *
 * <p>On the server, {@code BackpackHelper.getBackpacks(player)} returns the real
 * backpack ItemStacks (with their {@code DataComponents.CONTAINER} contents).
 * This is the cornerstone of the server-side scanning approach.</p>
 */
public final class BackpackedServerCompat {
    private BackpackedServerCompat() {}

    private static final boolean LOADED =
            FabricLoader.getInstance().isModLoaded("backpacked");

    /**
     * Returns the player's equipped backpack ItemStacks on the <b>server</b>.
     * Each non-empty stack's {@code DataComponents.CONTAINER} holds the backpack
     * inventory contents.  Returns an empty list if Backpacked is absent or
     * anything goes wrong.
     */
    public static List<ItemStack> getBackpackStacks(Player player) {
        if (!LOADED || player == null) return Collections.emptyList();
        try {
            return Accessor.getStacks(player);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    /** Loaded only when Backpacked is present \u2014 prevents NoClassDefFoundError. */
    private static final class Accessor {
        static List<ItemStack> getStacks(Player player) {
            return com.mrcrayfish.backpacked.BackpackHelper.getBackpacks(player);
        }
    }
}
