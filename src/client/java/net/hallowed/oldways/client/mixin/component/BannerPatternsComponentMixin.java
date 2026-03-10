package net.hallowed.oldways.client.mixin.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/**
 * Widen the BannerPatternsComponent codec validation so stacks may contain up to 16 layers.
 * This affects banners AND shields (since shields copy the component).
 */
@Environment(EnvType.CLIENT)
@Mixin(BannerPatternLayers.class)
public abstract class BannerPatternsComponentMixin {

    @ModifyConstant(
            method = { "addToTooltip" },
            constant = @Constant(intValue = 6),
            require = 0
    )
    private int oldways$raiseTooltipCap(int original) {
        return 16;
    }
}
