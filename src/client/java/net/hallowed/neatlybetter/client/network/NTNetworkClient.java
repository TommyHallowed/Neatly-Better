package net.hallowed.neatlybetter.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.hallowed.neatlybetter.client.util.LapisClientUtil;
import net.hallowed.neatlybetter.config.ShieldDelayHolder;
import net.hallowed.neatlybetter.network.NTNetwork;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class NTNetworkClient {
    private NTNetworkClient() {}

    public static void registerClient(Consumer<NTNetwork.EnderCheckResponse> onResponse) {
        ClientPlayNetworking.registerGlobalReceiver(NTNetwork.EnderCheckResponse.ID,
                (payload, ctx) -> ctx.client().execute(() -> onResponse.accept(payload)));

        ClientPlayNetworking.registerGlobalReceiver(NTNetwork.ShieldDelaySyncPayload.ID,
                (payload, ctx) -> ctx.client().execute(() ->
                        ShieldDelayHolder.setShieldRaiseDelay(payload.shieldRaiseDelay())));

        ClientPlayNetworking.registerGlobalReceiver(NTNetwork.LapisCountPayload.ID,
                (payload, ctx) -> ctx.client().execute(() ->
                        LapisClientUtil.syncLapisToClient(payload.lapisCount(), payload.pos())));
    }

    public static void sendEnderCheck() {
        ClientPlayNetworking.send(new NTNetwork.EnderCheckRequest());
    }

    public static void sendArmorSwap(int containerId, int sourceSlotIndex) {
        ClientPlayNetworking.send(new NTNetwork.ArmorSwapRequest(containerId, sourceSlotIndex));
    }

    public static void sendQuiverSelect(int slotIndex, int selectedItem) {
        ClientPlayNetworking.send(new NTNetwork.SelectQuiverItemPacket(slotIndex, selectedItem));
    }
}
