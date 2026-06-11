package net.hallowed.neatlybetter.util;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LapisVariables {
    private LapisVariables() {}

    public static final Map<UUID, BlockPos> lastEnchantingTableInteraction = new ConcurrentHashMap<>();
}