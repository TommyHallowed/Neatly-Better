package net.hallowed.neatlybetter.client.feature.locator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import net.hallowed.neatlybetter.client.util.ModTextures;

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
import org.jetbrains.annotations.NotNull;

public final class WaypointTracking {
    private WaypointTracking() {}

    public static final List<ClientWaypoint> WAYPOINTS = new ArrayList<>();

    private static final boolean SCAN_INVENTORIES = true;
    private static final boolean SHOW_LODESTONE   = true;
    private static final int     RECOVERY_COLOR   = 0xFFFF5555;
    private static final int     LODESTONE_COLOR  = 0xFF55AAFF;
    private static boolean foundRecoveryCompass;


    private record Node(ItemStack stack, int depth) {}

    @SuppressWarnings("SameReturnValue")
    public static List<ClientWaypoint> update(Player player) {
        WAYPOINTS.clear();
        foundRecoveryCompass = false;
        if (player == null) {
            DeathTracker.reset();
            return WAYPOINTS;
        }

        final ResourceKey<@NotNull Level> dim = player.level().dimension();

        final List<ItemStack> roots = new ArrayList<>(46);
        final NonNullList<@NotNull ItemStack> main = player.getInventory().getNonEquipmentItems();
        roots.addAll(main);
        final ItemStack off = player.getOffhandItem();
        if (!off.isEmpty()) roots.add(off);

        for (ItemStack s : roots) {
            if (!s.isEmpty()) scanStackIterative(player, dim, s);
        }

        if (SCAN_INVENTORIES) {
            var ender = player.getEnderChestInventory();
            for (int i = 0, n = ender.getContainerSize(); i < n; i++) {
                ItemStack s = ender.getItem(i);
                if (!s.isEmpty()) scanStackIterative(player, dim, s);
            }
        }

        EnderWaypointsClient.appendForDimension(dim, WAYPOINTS);

        BackpackWaypointsClient.appendForDimension(dim, WAYPOINTS);

        GlobalPos deathLoc = player.getLastDeathLocation().orElse(null);
        boolean showTimedDeath = DeathTracker.tick(deathLoc);
        if (!foundRecoveryCompass && showTimedDeath
                && deathLoc != null && deathLoc.dimension() == dim) {
            WAYPOINTS.add(new ClientWaypoint(
                    Vec3.atCenterOf(deathLoc.pos()),
                    Optional.empty(),
                    ModTextures.STYLE_DEATH,
                    Optional.of(RECOVERY_COLOR)
            ));
        }
        return WAYPOINTS;
    }

    private static void scanStackIterative(Player player, ResourceKey<@NotNull Level> dim, ItemStack root) {
        int scanned = 0;
        final Deque<Node> work = new ArrayDeque<>();
        work.add(new Node(root, 0));

        while (!work.isEmpty() && scanned < 4096) {
            final Node node = work.removeLast();
            final ItemStack stack = node.stack();
            final int depth = node.depth();
            if (stack == null || stack.isEmpty()) continue;

            scanned++;
            if (stack.is(Items.RECOVERY_COMPASS)) {
                foundRecoveryCompass = true;
                player.getLastDeathLocation().ifPresent(last -> {
                    if (last.dimension() == dim) {
                        WaypointTracking.WAYPOINTS.add(new ClientWaypoint(
                                Vec3.atCenterOf(last.pos()),
                                label(stack),
                                ModTextures.STYLE_DEATH,
                                ColorHandler.getColor(stack).or(() -> Optional.of(RECOVERY_COLOR))
                        ));
                    }
                });
            }

            if (SHOW_LODESTONE) {
                final LodestoneTracker lc = stack.get(DataComponents.LODESTONE_TRACKER);
                if (lc != null && lc.target().isPresent()) {
                    final GlobalPos pos = lc.target().get();
                    if (pos.dimension() == dim) {
                        WaypointTracking.WAYPOINTS.add(new ClientWaypoint(
                                Vec3.atCenterOf(pos.pos()),
                                label(stack),
                                ModTextures.STYLE_LODESTONE,
                                ColorHandler.getColor(stack).or(() -> Optional.of(LODESTONE_COLOR))
                        ));
                    }
                }
            }

            if (!SCAN_INVENTORIES || depth >= 16) continue;


            final BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (bundle != null) {
                bundle.itemCopyStream().forEach(child -> {
                    if (!child.isEmpty()) {
                        work.addLast(new Node(child, depth + 1));
                    }
                });
            }

            final ItemContainerContents container = stack.get(DataComponents.CONTAINER);
            if (container != null) {
                container.stream().forEach(child -> {
                    if (!child.isEmpty()) {
                        work.addLast(new Node(child, depth + 1));
                    }
                });
            }

        }

    }

    private static Optional<Component> label(ItemStack stack) {
        Component t = stack.get(DataComponents.CUSTOM_NAME);
        if (t == null) t = stack.get(DataComponents.ITEM_NAME);
        return ColorHandler.removeColorCode(t);
    }
}
