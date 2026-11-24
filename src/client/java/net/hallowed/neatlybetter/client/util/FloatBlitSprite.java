package net.hallowed.neatlybetter.client.util;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;

public interface FloatBlitSprite {
    /** Whole sprite at a fractional GUI position. */
    void neatlybetter$blitSpriteFloat(RenderPipeline pipeline, Identifier sprite,
                                      float x, float y, int width, int height);

    /**
     * Part of a sprite ({@code width}×{@code height} pixels starting at {@code textureX/Y} of a
     * {@code spriteWidth}×{@code spriteHeight} sprite) at a fractional GUI position, like vanilla's sub-sprite blit.
     */
    void neatlybetter$blitSpriteFloat(RenderPipeline pipeline, Identifier sprite,
                                      int spriteWidth, int spriteHeight, int textureX, int textureY,
                                      float x, float y, int width, int height);
}
