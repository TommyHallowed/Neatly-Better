package net.hallowed.oldways.init;

import com.mojang.serialization.Codec;
import net.hallowed.oldways.api.OWRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.NotNull;

/** Registers data components (e.g., emissive trim flag). */
public final class ModDataComponents {
    private ModDataComponents() {}

    /** Boolean flag to mark armor trims as emissive. */
    public static final DataComponentType<@NotNull Boolean> EMISSIVE_TRIM = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            OWRegistry.id("emissive_trim"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    /** Boolean flag to mark armor trims as pulsing (Echo Shard). */
    public static final DataComponentType<@NotNull Boolean> PULSING_TRIM = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            OWRegistry.id("pulsing_trim"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    /** No-op hook if you prefer calling something in your entrypoint. */
    public static void init() { /* class-load ensures registration */ }
}
