package net.hallowed.neatlybetter.init;

import com.mojang.serialization.Codec;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.hallowed.neatlybetter.util.DeathSlotData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DataComponentType<@NotNull Boolean> EMISSIVE_TRIM = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            NTRegistry.id("emissive_trim"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final DataComponentType<@NotNull Boolean> PULSING_TRIM = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            NTRegistry.id("pulsing_trim"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final DataComponentType<@NotNull CustomData> WOLF_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            NTRegistry.id("wolf_data"),
            DataComponentType.<CustomData>builder()
                    .persistent(CustomData.CODEC)
                    .networkSynchronized(CustomData.STREAM_CODEC)
                    .build()
    );

    public static final DataComponentType<@NotNull DeathSlotData> DEATH_SLOT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            NTRegistry.id("death_slot"),
            DataComponentType.<DeathSlotData>builder()
                    .persistent(DeathSlotData.CODEC)
                    .networkSynchronized(DeathSlotData.STREAM_CODEC)
                    .build()
    );

    public static void init() {}
}
