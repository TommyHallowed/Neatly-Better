package net.hallowed.neatlybetter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record DeathSlotData(UUID owner, int slot) {
    public static final Codec<DeathSlotData> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(DeathSlotData::owner),
            Codec.INT.fieldOf("slot").forGetter(DeathSlotData::slot)
    ).apply(i, DeathSlotData::new));

    public static final StreamCodec<ByteBuf, DeathSlotData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DeathSlotData::owner,
            ByteBufCodecs.VAR_INT, DeathSlotData::slot,
            DeathSlotData::new
    );
}