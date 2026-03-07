package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.util.ItemEntityRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public class ItemEntityRenderStateMixin implements ItemEntityRenderStateAccessor {
    @Unique private ItemStack olditems$stack;

    // store world-space X/Z for yaw-only facing
    @Unique private double olditems$x;
    @Unique private double olditems$z;

    @Override
    public ItemStack olditems$getStack() {
        return olditems$stack;
    }

    @Override
    public void olditems$setStack(ItemStack stack) {
        this.olditems$stack = stack;
    }

    @Override
    public double olditems$getX() {
        return olditems$x;
    }

    @Override
    public double olditems$getZ() {
        return olditems$z;
    }

    @Override
    public void olditems$setXZ(double x, double z) {
        this.olditems$x = x;
        this.olditems$z = z;
    }
}
