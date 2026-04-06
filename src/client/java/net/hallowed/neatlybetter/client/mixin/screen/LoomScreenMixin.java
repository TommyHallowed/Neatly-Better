package net.hallowed.neatlybetter.client.mixin.screen;

import net.minecraft.client.gui.screens.inventory.LoomScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin {

    @ModifyConstant(
            method = {
                    "containerChanged",
                    "extractBackground",
                    "extractBannerOnButton"
            },
            constant = @Constant(intValue = 6),
            require = 0
    )
    private int neatlybetter$raiseClientPatternCap(int original) {
        return 16;
    }
}
