package net.hallowed.neatlybetter.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class LapisUtil {
    private LapisUtil() {}

    public static @Nullable EnchantingTableBlockEntity getEnchantingTableBlockEntity(Player player) {
        if (player == null) return null;

        Level level = player.level();
        UUID uuid = player.getUUID();
        BlockPos tablePos = null;

        if (LapisVariables.lastEnchantingTableInteraction.containsKey(uuid)) {
            tablePos = LapisVariables.lastEnchantingTableInteraction.get(uuid).immutable();
        } else {
            BlockPos playerPos = player.blockPosition();
            for (BlockPos candidate : BlockPos.betweenClosed(
                    playerPos.getX() - 8, playerPos.getY() - 8, playerPos.getZ() - 8,
                    playerPos.getX() + 8, playerPos.getY() + 8, playerPos.getZ() + 8)) {
                if (level.getBlockState(candidate).getBlock() instanceof EnchantingTableBlock) {
                    tablePos = candidate.immutable();
                    break;
                }
            }
        }

        if (tablePos != null) {
            BlockEntity be = level.getBlockEntity(tablePos);
            if (be instanceof EnchantingTableBlockEntity enchTable) {
                return enchTable;
            }
        }
        return null;
    }

    public static void saveLapisCount(Level level, EnchantingTableBlockEntity enchTable, int lapisCount) {
        DataComponentMap existing = enchTable.components();
        DataComponentMap.Builder builder = DataComponentMap.builder().addAll(existing);

        if (lapisCount <= 0) {
            builder.set(DataComponents.MAX_STACK_SIZE, null);
        } else {
            builder.set(DataComponents.MAX_STACK_SIZE, lapisCount);
        }

        enchTable.setComponents(builder.build());
        enchTable.setChanged();
    }

    public static int getLapisCount(Level level, EnchantingTableBlockEntity enchTable) {
        DataComponentMap map = enchTable.components();
        if (map.has(DataComponents.MAX_STACK_SIZE)) {
            return map.get(DataComponents.MAX_STACK_SIZE);
        }
        return 0;
    }
}