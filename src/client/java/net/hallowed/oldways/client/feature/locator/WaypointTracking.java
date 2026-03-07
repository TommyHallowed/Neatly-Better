/*
 * The Old Ways - WaypointTracking
 * Hardened against cyclic container graphs and deep nesting.
 */
package net.hallowed.oldways.client.feature.locator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class WaypointTracking {
    private WaypointTracking() {}

    public static final List<ClientWaypoint> WAYPOINTS = new ArrayList<>();

    private static final boolean SCAN_INVENTORIES = true;
    private static final boolean SHOW_LODESTONE   = true;
    private static final int     RECOVERY_COLOR   = 0xFFFF5555;
    private static final int     LODESTONE_COLOR  = 0xFF55AAFF;

    /** Safety: prevent pathological nesting / cycles from burning CPU or crashing. */
    private static final int MAX_NESTING_DEPTH   = 16;    // deep enough for real gameplay, shallow enough for safety
    private static final int MAX_SCANNED_ITEMS   = 4096;  // hard budget per update call

    /** Work node used by the explicit DFS. */
    private record Node(ItemStack stack, int depth) {}

    @SuppressWarnings("SameReturnValue")
    public static List<ClientWaypoint> update(Player player) {
        WAYPOINTS.clear();
        if (player == null) return WAYPOINTS;

        final ResourceKey<Level> dim = player.level().dimension();

        // Collect initial roots (main inv + offhand)
        final List<ItemStack> roots = new ArrayList<>(46);
        final NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        if (main != null) roots.addAll(main);
        final ItemStack off = player.getOffhandItem();
        if (off != null && !off.isEmpty()) roots.add(off);

        // Scan roots
        for (ItemStack s : roots) {
            if (!s.isEmpty()) scanStackIterative(player, dim, s);
        }

        // Optional: ender chest (this can be large; we still guard in the scanner)
        if (SCAN_INVENTORIES) {
            var ender = player.getEnderChestInventory();
            for (int i = 0, n = ender.getContainerSize(); i < n; i++) {
                ItemStack s = ender.getItem(i);
                if (!s.isEmpty()) scanStackIterative(player, dim, s);
            }
        }

        // External client waypoints for this dimension
        EnderWaypointsClient.appendForDimension(dim, WAYPOINTS);
        return WAYPOINTS;
    }

    /**
     * Non-recursive DFS over container components. This is intentionally iterative to avoid SOE and
     * to enforce global budgets (depth and total scanned nodes).
     */
    private static void scanStackIterative(Player player, ResourceKey<Level> dim, ItemStack root) {
        int scanned = 0;
        final Deque<Node> work = new ArrayDeque<>();
        work.add(new Node(root, 0));

        while (!work.isEmpty() && scanned < MAX_SCANNED_ITEMS) {
            final Node node = work.removeLast();
            final ItemStack stack = node.stack();
            final int depth = node.depth();
            if (stack == null || stack.isEmpty()) continue;

            scanned++;
            // 1) Waypoints from special items on THIS stack
            if (stack.is(Items.RECOVERY_COMPASS)) {
                player.getLastDeathLocation().ifPresent(last -> {
                    if (last.dimension() == dim && last.pos() != null) {
                        WaypointTracking.WAYPOINTS.add(new ClientWaypoint(
                                Vec3.atCenterOf(last.pos()),
                                label(stack),
                                IdentifierHelper.style("death"),
                                ColorHandler.getColor(stack).or(() -> Optional.of(RECOVERY_COLOR))
                        ));
                    }
                });
            }

            if (SHOW_LODESTONE) {
                final LodestoneTracker lc = stack.get(DataComponents.LODESTONE_TRACKER);
                if (lc != null && lc.target().isPresent()) {
                    final GlobalPos pos = lc.target().get();
                    if (pos.dimension() == dim && pos.pos() != null) {
                        WaypointTracking.WAYPOINTS.add(new ClientWaypoint(
                                Vec3.atCenterOf(pos.pos()),
                                label(stack),
                                IdentifierHelper.style("lodestone"),
                                ColorHandler.getColor(stack).or(() -> Optional.of(LODESTONE_COLOR))
                        ));
                    }
                }
            }

            // 2) Expand children if allowed and within depth budget
            if (!SCAN_INVENTORIES || depth >= MAX_NESTING_DEPTH) continue;

            // Note: Do NOT use streams here; plain loops keep stack traces clean and predictable.
            // --- replace these two blocks in scanStackIterative ---

            final BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (bundle != null) {
                // BundleContentsComponent is NOT Iterable; iterate via stream()
                bundle.itemCopyStream().forEach(child -> {
                    if (child != null && !child.isEmpty()) {
                        work.addLast(new Node(child, depth + 1));
                    }
                });
            }

            final ItemContainerContents container = stack.get(DataComponents.CONTAINER);
            if (container != null) {
                // ContainerComponent is NOT Iterable; iterate via stream()
                container.stream().forEach(child -> {
                    if (child != null && !child.isEmpty()) {
                        work.addLast(new Node(child, depth + 1));
                    }
                });
            }

        }
        // If we hit the scan budget, that's OK: we displayed the "closest" interesting things first
        // (since roots come from player inv / ender chest). This avoids freezing on weird graphs.
    }

    private static Optional<Component> label(ItemStack stack) {
        Component t = stack.get(DataComponents.CUSTOM_NAME);
        if (t == null) t = stack.get(DataComponents.ITEM_NAME);
        return ColorHandler.removeColorCode(t);
    }

    static final class IdentifierHelper {
        static net.minecraft.resources.Identifier style(String path) {
            return net.minecraft.resources.Identifier.fromNamespaceAndPath("old-ways", path);
        }
    }
}
