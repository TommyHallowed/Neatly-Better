package net.hallowed.oldways.content;

import com.mojang.serialization.Codec;
import net.hallowed.TheOldWays;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Registers a boolean component used to mark trims as emissive. */
public final class ModDataComponents {

    public static final ComponentType<Boolean> EMISSIVE_TRIM = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(TheOldWays.MOD_ID, "emissive_trim"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)            // persistent storage
                    .packetCodec(PacketCodecs.BOOLEAN) // networking
                    .build()
    );

    private ModDataComponents() {}

    /** Call once from your mod initializer. */
    public static void init() { /* class-load hook */ }
}
