package net.hallowed.neatlybetter.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerPlacedBlockData extends SavedData {

    private static final Identifier DATA_NAME = Identifier.parse("neatlybetter_placed_blocks");

    private final LongOpenHashSet placedPositions;

    public static final Codec<PlayerPlacedBlockData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.listOf()
                            .fieldOf("placed")
                            .forGetter(d -> new LongArrayList(d.placedPositions))
            ).apply(instance, PlayerPlacedBlockData::new)
    );


    public static SavedDataType<@NotNull PlayerPlacedBlockData> type() {
        return new SavedDataType<>(DATA_NAME, PlayerPlacedBlockData::new, CODEC, null);
    }

    public PlayerPlacedBlockData() {
        this.placedPositions = new LongOpenHashSet();
    }

    private PlayerPlacedBlockData(List<Long> positions) {
        this.placedPositions = new LongOpenHashSet(positions);
    }

    public static PlayerPlacedBlockData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(type());
    }

    public void add(BlockPos pos) {
        placedPositions.add(pos.asLong());
        setDirty();
    }

    public boolean remove(BlockPos pos) {
        boolean removed = placedPositions.remove(pos.asLong());
        if (removed) setDirty();
        return removed;
    }
}