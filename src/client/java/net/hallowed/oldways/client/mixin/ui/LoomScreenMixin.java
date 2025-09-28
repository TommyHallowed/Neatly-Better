package net.hallowed.oldways.client.mixin.ui;

import net.minecraft.client.gui.screen.ingame.LoomScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raise UI-side banner pattern cap from 6 -> 16 so the result/preview/buttons
 * don't get hidden/disabled after 6 layers.
 */
@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin {

    @ModifyConstant(
            method = {
                    "onInventoryChanged", // called when slots change
                    "drawBackground",     // guards rendering paths in some mappings
                    "render"              // fallback: guards in render paths in others
            },
            constant = @Constant(intValue = 6),
            require = 0 // tolerate mapping differences
    )
    private int oldways$raiseClientPatternCap(int original) {
        return 16;
    }
}
