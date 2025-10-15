package net.hallowed.oldways.mixin.screen;

import net.minecraft.screen.LoomScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;


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