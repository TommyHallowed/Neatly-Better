package net.hallowed.oldways.content;

import com.mojang.serialization.Codec;
import net.hallowed.oldways.api.OWRegistry;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Registers data components (e.g., emissive trim flag). */
public final class ModDataComponents {
    private ModDataComponents() {}

    /** Boolean flag to mark armor trims as emissive. */
    public static final ComponentType<Boolean> EMISSIVE_TRIM = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            OWRegistry.id("emissive_trim"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOLEAN)
                    .build()
    );

    /** No-op hook if you prefer calling something in your entrypoint. */
    public static void init() { /* class-load ensures registration */ }
}
