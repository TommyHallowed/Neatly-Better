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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WaypointTracking {
    private WaypointTracking() {}

    public static final List<ClientWaypoint> WAYPOINTS = new ArrayList<>();

    private static final boolean SCAN_INVENTORIES = true;
    private static final boolean SHOW_LODESTONE   = true;
    private static final int     RECOVERY_COLOR   = 0xFFFF5555;
    private static final int     LODESTONE_COLOR  = 0xFF55AAFF;

    @SuppressWarnings("SameReturnValue")
    public static List<ClientWaypoint> update(PlayerEntity player) {
        WAYPOINTS.clear();
        if (player == null) return WAYPOINTS;

        final RegistryKey<World> dim = player.getEntityWorld().getRegistryKey();

        List<ItemStack> roots = new ArrayList<>();
        DefaultedList<ItemStack> main = player.getInventory().getMainStacks();
        if (main != null) roots.addAll(main);
        ItemStack off = player.getOffHandStack();
        if (off != null) roots.add(off);
        for (ItemStack s : roots) addFromStack(player, dim, s, WAYPOINTS);

        if (SCAN_INVENTORIES) {
            var ender = player.getEnderChestInventory();
            for (int i = 0, n = ender.size(); i < n; i++) {
                ItemStack s = ender.getStack(i);
                if (!s.isEmpty()) addFromStack(player, dim, s, WAYPOINTS);
            }
        }

        EnderWaypointsClient.appendForDimension(dim, WAYPOINTS);
        return WAYPOINTS;
    }

    private static void addFromStack(PlayerEntity player, RegistryKey<World> dim, ItemStack stack, List<ClientWaypoint> out) {
        if (stack.isOf(Items.RECOVERY_COMPASS)) {
            player.getLastDeathPos().ifPresent(last -> {
                if (last.dimension() == dim && last.pos() != null) {
                    out.add(new ClientWaypoint(
                            Vec3d.ofCenter(last.pos()),
                            label(stack),
                            IdentifierHelper.style("death"),
                            ColorHandler.getColor(stack).or(() -> Optional.of(RECOVERY_COLOR))
                    ));
                }
            });
        }

        if (SHOW_LODESTONE) {
            LodestoneTrackerComponent lc = stack.get(DataComponentTypes.LODESTONE_TRACKER);
            if (lc != null && lc.target().isPresent()) {
                GlobalPos pos = lc.target().get();
                if (pos.dimension() == dim && pos.pos() != null) {
                    out.add(new ClientWaypoint(
                            Vec3d.ofCenter(pos.pos()),
                            label(stack),
                            IdentifierHelper.style("lodestone"),
                            ColorHandler.getColor(stack).or(() -> Optional.of(LODESTONE_COLOR))
                    ));
                }
            }
        }

        if (SCAN_INVENTORIES) {
            BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (bundle != null) bundle.stream().forEach(child -> addFromStack(player, dim, child, out));
            ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
            if (container != null) container.stream().forEach(child -> addFromStack(player, dim, child, out));
        }
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
