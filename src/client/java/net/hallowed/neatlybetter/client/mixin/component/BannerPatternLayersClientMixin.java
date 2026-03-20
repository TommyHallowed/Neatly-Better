package net.hallowed.neatlybetter.client.mixin.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.world.level.block.entity.BannerPatternLayers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Environment(EnvType.CLIENT)
@Mixin(BannerPatternLayers.class)
public abstract class BannerPatternLayersClientMixin {

    @ModifyConstant(
            method = { "addToTooltip" },
            constant = @Constant(intValue = 6),
            require = 0
    )
    private int neatlybetter$raiseTooltipCap(int original) {
        return 16;
    }
}
