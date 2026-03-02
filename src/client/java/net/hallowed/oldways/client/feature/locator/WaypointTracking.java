/*
 * The Old Ways - WaypointTracking
 * Hardened against cyclic container graphs and deep nesting.
 */
package net.hallowed.oldways.client.feature.locator;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

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
    public static List<ClientWaypoint> update(PlayerEntity player) {
        WAYPOINTS.clear();
        if (player == null) return WAYPOINTS;

        final RegistryKey<World> dim = player.getEntityWorld().getRegistryKey();

        // Collect initial roots (main inv + offhand)
        final List<ItemStack> roots = new ArrayList<>(46);
        final DefaultedList<ItemStack> main = player.getInventory().getMainStacks();
        if (main != null) roots.addAll(main);
        final ItemStack off = player.getOffHandStack();
        if (off != null && !off.isEmpty()) roots.add(off);

        // Scan roots
        for (ItemStack s : roots) {
            if (!s.isEmpty()) scanStackIterative(player, dim, s);
        }

        // Optional: ender chest (this can be large; we still guard in the scanner)
        if (SCAN_INVENTORIES) {
            var ender = player.getEnderChestInventory();
            for (int i = 0, n = ender.size(); i < n; i++) {
                ItemStack s = ender.getStack(i);
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
    private static void scanStackIterative(PlayerEntity player, RegistryKey<World> dim, ItemStack root) {
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
            if (stack.isOf(Items.RECOVERY_COMPASS)) {
                player.getLastDeathPos().ifPresent(last -> {
                    if (last.dimension() == dim && last.pos() != null) {
                        WaypointTracking.WAYPOINTS.add(new ClientWaypoint(
                                Vec3d.ofCenter(last.pos()),
                                label(stack),
                                IdentifierHelper.style("death"),
                                ColorHandler.getColor(stack).or(() -> Optional.of(RECOVERY_COLOR))
                        ));
                    }
                });
            }

            if (SHOW_LODESTONE) {
                final LodestoneTrackerComponent lc = stack.get(DataComponentTypes.LODESTONE_TRACKER);
                if (lc != null && lc.target().isPresent()) {
                    final GlobalPos pos = lc.target().get();
                    if (pos.dimension() == dim && pos.pos() != null) {
                        WaypointTracking.WAYPOINTS.add(new ClientWaypoint(
                                Vec3d.ofCenter(pos.pos()),
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

            final BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (bundle != null) {
                // BundleContentsComponent is NOT Iterable; iterate via stream()
                bundle.stream().forEach(child -> {
                    if (child != null && !child.isEmpty()) {
                        work.addLast(new Node(child, depth + 1));
                    }
                });
            }

            final ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
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

    private static Optional<Text> label(ItemStack stack) {
        Text t = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (t == null) t = stack.get(DataComponentTypes.ITEM_NAME);
        return ColorHandler.removeColorCode(t);
    }

    static final class IdentifierHelper {
        static net.minecraft.util.Identifier style(String path) {
            return net.minecraft.util.Identifier.of("old-ways", path);
        }
    }
}
