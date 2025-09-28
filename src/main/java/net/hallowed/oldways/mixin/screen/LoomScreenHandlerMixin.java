package net.hallowed.oldways.mixin.screen;

import net.minecraft.screen.LoomScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raises the banner pattern cap in the loom from 6 -> 16.
 * Vanilla 1.21.8 checks `layers().size() >= 6` inside onContentChanged(...).
 */
@Mixin(LoomScreenHandler.class)
public abstract class LoomScreenHandlerMixin {

    @ModifyConstant(
            method = "onContentChanged(Lnet/minecraft/inventory/Inventory;)V",
            constant = @Constant(intValue = 6)
    )
    private int oldways$raisePatternCap(int original) {
        return 16;
    }
}