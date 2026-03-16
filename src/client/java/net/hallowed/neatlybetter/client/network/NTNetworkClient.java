package net.hallowed.neatlybetter.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.hallowed.neatlybetter.network.NTNetwork;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class NTNetworkClient {
    private NTNetworkClient() {}

    public static void registerClient(Consumer<NTNetwork.EnderCheckResponse> onResponse) {
        ClientPlayNetworking.registerGlobalReceiver(NTNetwork.EnderCheckResponse.ID,
                (payload, ctx) -> ctx.client().execute(() -> onResponse.accept(payload)));
    }

    public static void sendEnderCheck() {
        ClientPlayNetworking.send(new NTNetwork.EnderCheckRequest());
    }

    public static void sendArmorSwap(int containerId, int sourceSlotIndex) {
        ClientPlayNetworking.send(new NTNetwork.ArmorSwapRequest(containerId, sourceSlotIndex));
    }
}
