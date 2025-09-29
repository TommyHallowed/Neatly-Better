package net.hallowed.oldways.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hallowed.oldways.client.network.OldWaysNetworkClient;
import net.hallowed.oldways.network.OldWaysNetwork;

@Environment(EnvType.CLIENT)
public final class EnderCheckClient {
    private static volatile boolean hasCompassEnder = false;
    private static volatile boolean hasClockEnder   = false;

    public static void register() {
        // Receive S2C
        OldWaysNetworkClient.registerClient((OldWaysNetwork.EnderCheckResponse resp) -> {
            hasCompassEnder = resp.hasCompass();
            hasClockEnder   = resp.hasClock();
        });

        // Ask once on join (in case JOIN push is delayed on some stacks)
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                client.execute(OldWaysNetworkClient::sendEnderCheck));

        // Reset on disconnect so stale values don't bleed into SP/new servers
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            hasCompassEnder = false;
            hasClockEnder   = false;
        });
    }

    public static boolean enderHasCompass() { return hasCompassEnder; }
    public static boolean enderHasClock()   { return hasClockEnder; }

    private EnderCheckClient() {}
}
