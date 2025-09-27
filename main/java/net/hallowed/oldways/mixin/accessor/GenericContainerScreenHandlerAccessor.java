package net.hallowed.oldways.mixin.accessor;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GenericContainerScreenHandler.class)
public interface GenericContainerScreenHandlerAccessor {
    @Accessor("inventory")
    Inventory oldways$getInventory();
}
