package net.hallowed.neatlybetter.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.hallowed.neatlybetter.util.LapisUtil;
import net.hallowed.neatlybetter.util.LapisVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class LapisClientUtil {
    private LapisClientUtil() {}

    public static void syncLapisToClient(int lapisCount, BlockPos tablePos) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        UUID uuid = player.getUUID();
        if (!LapisVariables.lastEnchantingTableInteraction.containsKey(uuid)) return;
        if (!tablePos.equals(LapisVariables.lastEnchantingTableInteraction.get(uuid))) return;

        Level level = player.level();
        BlockEntity be = level.getBlockEntity(tablePos);
        if (!(be instanceof EnchantingTableBlockEntity enchTable)) return;

        LapisUtil.saveLapisCount(level, enchTable, lapisCount);

        if (player.containerMenu instanceof EnchantmentMenu enchMenu) {
            ItemStack stack = lapisCount > 0
                    ? new ItemStack(Items.LAPIS_LAZULI, lapisCount)
                    : ItemStack.EMPTY;
            enchMenu.enchantSlots.setItem(1, stack);
        }
    }
}