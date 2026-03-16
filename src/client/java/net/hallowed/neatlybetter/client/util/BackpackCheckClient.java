package net.hallowed.neatlybetter.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.hallowed.neatlybetter.network.NTNetwork;

@Environment(EnvType.CLIENT)
public final class BackpackCheckClient {
    private static volatile boolean hasCompass = false;
    private static volatile boolean hasClock = false;
    private static volatile boolean hasRecoveryCompass = false;

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(NTNetwork.BackpackCheckResponse.ID,
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
    @SuppressWarnings("unused")
    public static boolean backpackHasRecoveryCompass() { return hasRecoveryCompass; }
    public static boolean backpackHasAnyCompass()      { return hasCompass || hasRecoveryCompass; }

    private BackpackCheckClient() {}
}
