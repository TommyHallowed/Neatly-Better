package net.hallowed.oldways.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class EnderCheckPackets {
    public static final String MOD_ID = "old_ways"; // <-- your actual mod id

    // C2S: empty request
    public record EnderCheckRequest() implements CustomPayload {
        public static final Id<EnderCheckRequest> ID =
                new Id<>(Identifier.of(MOD_ID, "ender_check_request"));
        public static final PacketCodec<RegistryByteBuf, EnderCheckRequest> CODEC =
                PacketCodec.unit(new EnderCheckRequest());
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // S2C: answer with two booleans
    public record EnderCheckResponse(boolean hasCompass, boolean hasClock) implements CustomPayload {
        public static final Id<EnderCheckResponse> ID =
                new Id<>(Identifier.of(MOD_ID, "ender_check_response"));
        public static final PacketCodec<RegistryByteBuf, EnderCheckResponse> CODEC =
                PacketCodec.tuple(PacketCodecs.BOOLEAN, EnderCheckResponse::hasCompass,
                        PacketCodecs.BOOLEAN, EnderCheckResponse::hasClock,
                        EnderCheckResponse::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    private EnderCheckPackets() {}
}
