package net.hallowed.oldways.client.feature.locator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class EnderWaypointsClient {

    private static final List<Entry> CACHE = new ArrayList<>();

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OldWaysNetwork.EnderLodestones.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        CACHE.clear();
                        for (OldWaysNetwork.LodestoneEntry e : payload.entries()) {
                            RegistryKey<World> dim = RegistryKey.of(RegistryKeys.WORLD, e.dim());
                            Vec3d pos = new Vec3d(e.x() + 0.5, e.y() + 0.5, e.z() + 0.5);
                            Integer color = e.color() >= 0 ? e.color() : null;
                            String lbl = e.label();
                            CACHE.add(new Entry(dim, pos, lbl == null || lbl.isBlank() ? null : lbl, color));
                        }
                    });
                });
    }

    public static void appendForDimension(RegistryKey<World> dim, List<ClientWaypoint> out) {
        if (CACHE.isEmpty()) return;
        for (Entry e : CACHE) {
            if (!Objects.equals(e.dim, dim)) continue;
            out.add(new ClientWaypoint(
                    e.pos,
                    e.label == null ? java.util.Optional.empty() : java.util.Optional.of(Text.literal(e.label)),
                    net.minecraft.util.Identifier.of("old-ways", "lodestone"),
                    e.color == null ? java.util.Optional.empty() : java.util.Optional.of(e.color)
            ));
        }
    }

    private record Entry(RegistryKey<World> dim, Vec3d pos, String label, Integer color) {}
    private EnderWaypointsClient() {}
}
