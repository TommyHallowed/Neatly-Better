package net.hallowed.neatlybetter.init;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hallowed.neatlybetter.api.NTRegistry;

import net.hallowed.neatlybetter.content.component.QuiverContents;
import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.data.ChunkCarpetPatterns;
import net.hallowed.neatlybetter.data.DeathSlotData;
import net.hallowed.neatlybetter.data.VaultReopenData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

public final class ModData {
    private ModData() {}

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

    public static final DataComponentType<@NotNull QuiverContents> QUIVER_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, NTRegistry.id("quiver_contents"),
            DataComponentType.<QuiverContents>builder()
                    .persistent(QuiverContents.CODEC)
                    .networkSynchronized(QuiverContents.STREAM_CODEC)
                    .build()
    );

    public static final AttachmentType<@NotNull VaultReopenData> VAULT_REOPEN =
            AttachmentRegistry.<VaultReopenData>builder()
                    .persistent(VaultReopenData.CODEC)
                    .initializer(VaultReopenData::new)
                    .buildAndRegister(NTRegistry.id("vault_reopen"));

    public static final DataComponentType<@NotNull CarpetPatternData> CARPET_PATTERNS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            NTRegistry.id("carpet_patterns"),
            DataComponentType.<CarpetPatternData>builder()
                    .persistent(CarpetPatternData.CODEC)
                    .networkSynchronized(CarpetPatternData.STREAM_CODEC)
                    .build()
    );

    public static final AttachmentType<@NotNull ChunkCarpetPatterns> CARPET_PATTERN_DATA =
            AttachmentRegistry.<ChunkCarpetPatterns>builder()
                    .persistent(ChunkCarpetPatterns.CODEC)
                    .initializer(ChunkCarpetPatterns::new)
                    .syncWith(ChunkCarpetPatterns.STREAM_CODEC, AttachmentSyncPredicate.all())
                    .buildAndRegister(NTRegistry.id("carpet_pattern_data"));

    public static void init() {}
}
