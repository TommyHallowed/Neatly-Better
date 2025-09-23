package net.hallowed.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hallowed.network.EnderCheckPackets;

@Environment(EnvType.CLIENT)
public final class EnderCheckClient {
    private static boolean hasCompassEnder = false;
    private static boolean hasClockEnder   = false;
    private static long lastRequestMs = 0;

    public static void register() {
        // receive updates
        ClientPlayNetworking.registerGlobalReceiver(
                EnderCheckPackets.EnderCheckResponse.ID,
                (payload, context) -> {
                    hasCompassEnder = payload.hasCompass();
                    hasClockEnder   = payload.hasClock();
                });

        // ping server ~every second while in-game
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) return;
            long now = System.currentTimeMillis();
            if (now - lastRequestMs >= 1000) {
                lastRequestMs = now;
                ClientPlayNetworking.send(new EnderCheckPackets.EnderCheckRequest());
            }
        });
    }

    public static boolean enderHasCompass() { return hasCompassEnder; }
    public static boolean enderHasClock()   { return hasClockEnder; }

    private EnderCheckClient() {}
}
