package net.hallowed.oldways.client.util;

import net.minecraft.item.ItemStack;

public interface ItemEntityRenderStateAccessor {
    ItemStack olditems$getStack();
    void olditems$setStack(ItemStack stack);

    double olditems$getX();
    double olditems$getZ();
    void olditems$setXZ(double x, double z);
}
