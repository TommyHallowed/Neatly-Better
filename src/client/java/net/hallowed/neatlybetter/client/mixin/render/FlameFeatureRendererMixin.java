package net.hallowed.neatlybetter.client.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.hallowed.neatlybetter.client.feature.soulfire.SoulFireRendering;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Third-person flames: soul fire sprites for players burning with soul fire. */
@Mixin(FlameFeatureRenderer.class)
public abstract class FlameFeatureRendererMixin {

    @WrapOperation(
            method = "buildGroup",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer;prepare(Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer$Submit;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V")
    )
    private void neatlybetter$soulFlames(FlameFeatureRenderer self, FlameFeatureRenderer.Submit submit, VertexConsumer buffer,
                                         TextureAtlasSprite fire1, TextureAtlasSprite fire2, Operation<Void> original,
                                         @Local(argsOnly = true) FeatureFrameContext context) {
        if (Boolean.TRUE.equals(submit.entityRenderState().getData(SoulFireRendering.SOUL_FIRE))) {
            fire1 = context.atlasManager().get(SoulFireRendering.SOUL_FIRE_0);
            fire2 = context.atlasManager().get(SoulFireRendering.SOUL_FIRE_1);
        }
        original.call(self, submit, buffer, fire1, fire2);
    }
}
