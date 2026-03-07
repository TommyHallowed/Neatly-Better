package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.util.ItemEntityStackHolder;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public abstract class ItemEntityRenderStateMixin implements ItemEntityStackHolder {
    @Unique private ItemStack oldways$stack = ItemStack.EMPTY;

    @Override public void oldways$setStack(ItemStack stack) { this.oldways$stack = stack; }
    @Override public ItemStack oldways$getStack() { return this.oldways$stack; }
}