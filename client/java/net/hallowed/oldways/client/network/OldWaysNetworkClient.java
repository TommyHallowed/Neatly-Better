package net.hallowed.oldways.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hallowed.oldways.network.OldWaysNetwork;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class OldWaysNetworkClient {
    private OldWaysNetworkClient() {}

    /** Register the S2C receiver. Call from your client init. */
    public static void registerClient(Consumer<OldWaysNetwork.EnderCheckResponse> onResponse) {
        ClientPlayNetworking.registerGlobalReceiver(OldWaysNetwork.EnderCheckResponse.ID,
                (payload, ctx) -> ctx.client().execute(() -> onResponse.accept(payload)));
    }

    /** Send a C2S ender-check request (use on a client tick). */
    public static void sendEnderCheck() {
        ClientPlayNetworking.send(new OldWaysNetwork.EnderCheckRequest());
    }
}
