package net.hallowed.oldways.client.mixin.screen;

import net.minecraft.client.gui.screen.ingame.LoomScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin {

    @ModifyConstant(
            method = {
                    "onInventoryChanged",
                    "drawBackground",
                    "render"
            },
            constant = @Constant(intValue = 6),
            require = 0
    )
    private int oldways$raiseClientPatternCap(int original) {
        return 16;
    }
}
