package net.hallowed.oldways.content.block;

import net.minecraft.block.BannerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.DyeColor;

/**
 * Standing rainbow banner block.
 * Behaves like vanilla BannerBlock; we pass WHITE as the base color.
 * Rendering of the base cloth is swapped by your client mixin.
 */
public class RainbowBannerBlock extends BannerBlock {
    public RainbowBannerBlock(AbstractBlock.Settings settings) {
        super(DyeColor.WHITE, settings);
    }
}
