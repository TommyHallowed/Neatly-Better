package net.hallowed.oldways.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hallowed.oldways.network.OldWaysNetwork;

/**
 * Client-side cache for backpack scan results pushed from the server.
 * Mirrors the pattern used by {@link EnderCheckClient} for ender chests.
 *
 * <p>The server periodically scans the player's Backpacked backpack inventories
 * and sends boolean flags + lodestone data. This class caches the boolean flags.</p>
 */
@Environment(EnvType.CLIENT)
public final class BackpackCheckClient {
    private static volatile boolean hasCompass = false;
    private static volatile boolean hasClock = false;
    private static volatile boolean hasRecoveryCompass = false;

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OldWaysNetwork.BackpackCheckResponse.ID,
                (payload, ctx) -> ctx.client().execute(() -> {
                    hasCompass = payload.hasCompass();
                    hasClock = payload.hasClock();
                    hasRecoveryCompass = payload.hasRecoveryCompass();
                }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            hasCompass = false;
            hasClock = false;
            hasRecoveryCompass = false;
        });
    }

    public static boolean backpackHasCompass()         { return hasCompass; }
    public static boolean backpackHasClock()           { return hasClock; }
    public static boolean backpackHasRecoveryCompass() { return hasRecoveryCompass; }
    public static boolean backpackHasAnyCompass()      { return hasCompass || hasRecoveryCompass; }

    private BackpackCheckClient() {}
}
