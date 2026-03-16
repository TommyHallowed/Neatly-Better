package net.hallowed.neatlybetter.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.hallowed.neatlybetter.network.NTNetwork;

@Environment(EnvType.CLIENT)
public final class EnderCheckClient {
    private static volatile boolean hasCompassEnder = false;
    private static volatile boolean hasClockEnder   = false;

    public static void register() {
        net.hallowed.neatlybetter.client.network.NTNetworkClient.registerClient((NTNetwork.EnderCheckResponse resp) -> {
            hasCompassEnder = resp.hasCompass();
            hasClockEnder   = resp.hasClock();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                client.execute(net.hallowed.neatlybetter.client.network.NTNetworkClient::sendEnderCheck));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            hasCompassEnder = false;
            hasClockEnder   = false;
        });
    }

    public static boolean enderHasCompass() { return hasCompassEnder; }
    public static boolean enderHasClock()   { return hasClockEnder; }

    private EnderCheckClient() {}
}
