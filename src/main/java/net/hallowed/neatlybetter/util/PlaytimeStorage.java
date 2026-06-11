package net.hallowed.neatlybetter.util;

import net.minecraft.world.level.storage.LevelSummary;

import java.util.WeakHashMap;

public final class PlaytimeStorage {
    private PlaytimeStorage() {}

    private static final WeakHashMap<LevelSummary, Long> TICKS = new WeakHashMap<>();

    public static void put(LevelSummary summary, long ticks) {
        TICKS.put(summary, ticks);
    }

    public static long get(LevelSummary summary) {
        return TICKS.getOrDefault(summary, -1L);
    }
}