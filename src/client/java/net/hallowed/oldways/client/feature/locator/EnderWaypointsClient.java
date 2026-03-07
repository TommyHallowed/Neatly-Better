package net.hallowed.oldways.client.feature.locator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class EnderWaypointsClient {

    private static final List<Entry> CACHE = new ArrayList<>();

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OldWaysNetwork.EnderLodestones.ID,
                (payload, context) -> context.client().execute(() -> {
                    CACHE.clear();
                    for (OldWaysNetwork.LodestoneEntry e : payload.entries()) {

                        // FIX 1: Translated RegistryKey.of(RegistryKeys.WORLD) to ResourceKey.create(Registries.DIMENSION)
                        ResourceKey<@NotNull Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, e.dim());

                        // FIX 2: Translated Vec3d to Vec3
                        Vec3 pos = new Vec3(e.x() + 0.5, e.y() + 0.5, e.z() + 0.5);

                        Integer color = e.color() >= 0 ? e.color() : null;
                        String lbl = e.label();
                        CACHE.add(new Entry(dim, pos, lbl == null || lbl.isBlank() ? null : lbl, color));
                    }
                }));
    }

    public static void appendForDimension(ResourceKey<@NotNull Level> dim, List<ClientWaypoint> out) {
        if (CACHE.isEmpty()) return;
        for (Entry e : CACHE) {
            if (!Objects.equals(e.dim, dim)) continue;
            out.add(new ClientWaypoint(
                    e.pos,
                    e.label == null ? java.util.Optional.empty() : java.util.Optional.of(Component.literal(e.label)),
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("old-ways", "lodestone"),
                    e.color == null ? java.util.Optional.empty() : java.util.Optional.of(e.color)
            ));
        }
    }

    private record Entry(ResourceKey<@NotNull Level> dim, Vec3 pos, String label, Integer color) {}
    private EnderWaypointsClient() {}
}