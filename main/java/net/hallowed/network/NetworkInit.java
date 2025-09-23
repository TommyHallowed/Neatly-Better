package net.hallowed.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class NetworkInit {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(
                EnderCheckPackets.EnderCheckRequest.ID, EnderCheckPackets.EnderCheckRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(
                EnderCheckPackets.EnderCheckResponse.ID, EnderCheckPackets.EnderCheckResponse.CODEC);
    }
    private NetworkInit() {}
}
