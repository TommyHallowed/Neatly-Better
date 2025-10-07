package net.hallowed.oldways.util;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

public final class PlacedBlocksState extends PersistentState {

    public static final PersistentStateType<PlacedBlocksState> TYPE =
            new PersistentStateType<>(
                    "oldways_placed_blocks",
                    PlacedBlocksState::new,
                    Codec.LONG.listOf().xmap(
                            list -> {
                                PlacedBlocksState s = new PlacedBlocksState();
                                for (Long v : list) s.placed.add(v.longValue());
                                return s;
                            },
                            s -> {
                                long[] arr = s.placed.toLongArray();
                                List<Long> out = new ArrayList<>(arr.length);
                                for (long v : arr) out.add(v);
                                return out;
                            }
                    ),
                    DataFixTypes.LEVEL
            );

    private final LongOpenHashSet placed = new LongOpenHashSet();

    public static PlacedBlocksState get(ServerWorld world) {
        PersistentStateManager psm = world.getPersistentStateManager();
        return psm.getOrCreate(TYPE);
    }

    public boolean has(long packed) { return placed.contains(packed); }
    public void add(long packed)    { if (placed.add(packed)) markDirty(); }
    public void remove(long packed) { if (placed.remove(packed)) markDirty(); }
}
