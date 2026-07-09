package net.hallowed.neatlybetter.util;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WoolCarpetBlock;

public final class CarpetLoomSupport {
    private CarpetLoomSupport() {}

    public static boolean isCarpetItem(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof WoolCarpetBlock;
    }
}