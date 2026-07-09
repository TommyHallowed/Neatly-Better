package net.hallowed.neatlybetter.data;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ChunkCarpetPatterns {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<BlockPos, CarpetPatternData> entries;
    private final Map<BlockPos, CarpetPatternData> pendingDrop = new HashMap<>();

    private record Entry(BlockPos pos, CarpetPatternData data) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                CarpetPatternData.CODEC.fieldOf("data").forGetter(Entry::data)
        ).apply(i, Entry::new));

        static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Entry::pos,
                CarpetPatternData.STREAM_CODEC, Entry::data,
                Entry::new
        );
    }

    public static final Codec<ChunkCarpetPatterns> CODEC = Entry.CODEC.listOf().xmap(
            list -> {
                ChunkCarpetPatterns result = new ChunkCarpetPatterns();
                for (Entry e : list) {
                    result.entries.put(e.pos().immutable(), e.data());
                }
                return result;
            },
            patterns -> {
                var list = patterns.entries.entrySet().stream()
                        .map(e -> new Entry(e.getKey(), e.getValue()))
                        .toList();
                LOGGER.debug("[NeatlyBetter/CarpetPatterns] Serializing {} carpet pattern(s) to chunk NBT",
                        list.size());
                return list;
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkCarpetPatterns> STREAM_CODEC =
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
                    list -> {
                        ChunkCarpetPatterns result = new ChunkCarpetPatterns();
                        for (Entry e : list) {
                            result.entries.put(e.pos().immutable(), e.data());
                        }
                        LOGGER.debug("[NeatlyBetter/CarpetPatterns] Synced {} carpet pattern(s) to client",
                                result.entries.size());
                        return result;
                    },
                    patterns -> patterns.entries.entrySet().stream()
                            .map(e -> new Entry(e.getKey(), e.getValue()))
                            .toList()
            );

    public ChunkCarpetPatterns() {
        this.entries = new HashMap<>();
    }

    public @Nullable CarpetPatternData get(BlockPos pos) {
        return entries.get(pos);
    }

    public CarpetPatternData getOrEmpty(BlockPos pos) {
        return entries.getOrDefault(pos, CarpetPatternData.EMPTY);
    }

    public boolean has(BlockPos pos) {
        return entries.containsKey(pos);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public Map<BlockPos, CarpetPatternData> getAll() {
        return Map.copyOf(entries);
    }

    public void set(BlockPos pos, @Nullable CarpetPatternData data) {
        if (data == null || data.isEmpty()) {
            remove(pos);
            return;
        }
        BlockPos immutable = pos.immutable();
        entries.put(immutable, data);
        LOGGER.debug("[NeatlyBetter/CarpetPatterns] Set pattern at {} ({} layer(s))",
                immutable, data.layerCount());
    }

    public void remove(BlockPos pos) {
        CarpetPatternData removed = entries.remove(pos);
        if (removed != null) {
            pendingDrop.put(pos.immutable(), removed);
            LOGGER.debug("[NeatlyBetter/CarpetPatterns] Removed pattern at {} (staged for drop)", pos);
        }
    }

    public @Nullable CarpetPatternData consumePendingDrop(BlockPos pos) {
        CarpetPatternData data = pendingDrop.remove(pos);
        if (data != null) {
            LOGGER.debug("[NeatlyBetter/CarpetPatterns] Consumed staged pattern at {} for drop", pos);
        }
        return data;
    }

    public ChunkCarpetPatterns withSet(BlockPos pos, @Nullable CarpetPatternData data) {
        ChunkCarpetPatterns copy = new ChunkCarpetPatterns();
        copy.entries.putAll(this.entries);
        copy.pendingDrop.putAll(this.pendingDrop);
        copy.set(pos, data);
        return copy;
    }

    public ChunkCarpetPatterns withRemove(BlockPos pos) {
        ChunkCarpetPatterns copy = new ChunkCarpetPatterns();
        copy.entries.putAll(this.entries);
        copy.pendingDrop.putAll(this.pendingDrop);
        copy.remove(pos);
        return copy;
    }
}