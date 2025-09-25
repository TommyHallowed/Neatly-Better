package net.hallowed.oldways.content.block;

import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.DyeColor;

/**
 * Wall-mounted rainbow banner block.
 * Behaves like vanilla WallBannerBlock; base color WHITE.
 * Rendering of the base cloth is swapped by your client mixin.
 */
public class RainbowWallBannerBlock extends WallBannerBlock {
    public RainbowWallBannerBlock(AbstractBlock.Settings settings) {
        super(DyeColor.WHITE, settings);
    }
}
