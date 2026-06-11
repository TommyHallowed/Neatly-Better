package net.hallowed.neatlybetter.client.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;

public interface FloatBlitSprite {
    void neatlybetter$blitSpriteFloat(RenderPipeline pipeline, Identifier sprite,
                                      float x, float y, int width, int height);
}