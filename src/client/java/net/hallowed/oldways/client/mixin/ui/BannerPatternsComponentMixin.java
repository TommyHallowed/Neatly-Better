package net.hallowed.oldways.client.mixin.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.type.BannerPatternsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/**
 * Widen the BannerPatternsComponent codec validation so stacks may contain up to 16 layers.
 * This affects banners AND shields (since shields copy the component).
 */
@Environment(EnvType.CLIENT)
@Mixin(BannerPatternsComponent.class)
public abstract class BannerPatternsComponentMixin {

    @ModifyConstant(
            method = { "appendTooltip" },
            constant = @Constant(intValue = 6),
            require = 0
    )
    private int oldways$raiseTooltipCap(int original) {
        return 16;
    }

    @Redirect(
            method = { "appendTooltip" },
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"),
            require = 0
    )
    private int oldways$widenMinForTooltip(int a, int b) {
        return Math.min(a, 16);
    }
}
