package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.accessor.EntityRenderStateAccessor;
import net.hallowed.oldways.client.render.SoulFireSprites;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.FireCommandRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireCommandRenderer.class)
public class FireCommandRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/render/command/BatchingRenderCommandQueue;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/texture/AtlasManager;)V", at = @At("HEAD"))
    private void oldways$noop(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, AtlasManager atlasManager, CallbackInfo ci) {
    }

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;Lnet/minecraft/client/texture/AtlasManager;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$renderSoulFire(MatrixStack.Entry matricesEntry, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, AtlasManager atlasManager, CallbackInfo ci) {
        // Do not consult spatial cache; prefer the render state's flag, otherwise fall back to the client player's remembered flag.
        boolean soul = false;
        try {
            if (renderState instanceof EntityRenderStateAccessor ers) {
                soul = ers.oldways$isSoulFire();
            } else {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.player instanceof net.hallowed.oldways.client.accessor.ClientPlayerEntityAccessor acc) {
                    soul = acc.oldways$isSoulFire();
                }
            }
        } catch (Throwable ignored) {
            if (renderState instanceof EntityRenderStateAccessor ers) soul = ers.oldways$isSoulFire();
        }

        Sprite sprite = soul ? atlasManager.getSprite(SoulFireSprites.FIRE_SOUL_0) : atlasManager.getSprite(ModelBaker.FIRE_0);
        Sprite sprite2 = soul ? atlasManager.getSprite(SoulFireSprites.FIRE_SOUL_1) : atlasManager.getSprite(ModelBaker.FIRE_1);
        float f = renderState.width * 1.4F;
        matricesEntry.scale(f, f, f);
        float g = 0.5F;
        float i = renderState.height / f;
        float j = 0.0F;
        matricesEntry.rotate(rotation);
        matricesEntry.translate(0.0F, 0.0F, 0.3F - (float)((int)i) * 0.02F);
        float k = 0.0F;
        int l = 0;

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        for(; i > 0.0F; ++l) {
            Sprite sprite3 = l % 2 == 0 ? sprite : sprite2;
            float m = sprite3.getMinU();
            float n = sprite3.getMinV();
            float o = sprite3.getMaxU();
            float p = sprite3.getMaxV();
            if (l / 2 % 2 == 0) {
                float q = o;
                o = m;
                m = q;
            }

            vertex(matricesEntry, vertexConsumer, -g - 0.0F, 0.0F - j, k, o, p);
            vertex(matricesEntry, vertexConsumer, g - 0.0F, 0.0F - j, k, m, p);
            vertex(matricesEntry, vertexConsumer, g - 0.0F, 1.4F - j, k, m, n);
            vertex(matricesEntry, vertexConsumer, -g - 0.0F, 1.4F - j, k, o, n);
            i -= 0.45F;
            j -= 0.45F;
            g *= 0.9F;
            k -= 0.03F;
        }

        ci.cancel();
    }

    @Unique
    private static void vertex(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v) {
        vertexConsumer.vertex(matricesEntry, x, y, z).color(-1).texture(u, v).overlay(0, 10).light(240).normal(matricesEntry, 0.0F, 1.0F, 0.0F);
    }
}
