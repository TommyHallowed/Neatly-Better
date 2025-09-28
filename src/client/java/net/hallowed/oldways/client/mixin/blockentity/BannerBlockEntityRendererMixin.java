package net.hallowed.oldways.client.mixin.blockentity;

import net.hallowed.oldways.content.ModBlocks;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.BannerPatternsComponent.Layer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerBlockEntityRenderer.class)
public abstract class BannerBlockEntityRendererMixin {
    @Unique private static final ThreadLocal<Boolean> OLDWAYS$OUR = ThreadLocal.withInitial(() -> false);

    @Shadow
    private static void renderLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay,
                                    ModelPart canvas, SpriteIdentifier textureId, DyeColor color) {}

    // mark block renders (standing/wall rainbow banners)
    @Inject(
            method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD")
    )
    private void oldways$begin(BannerBlockEntity be, float f, MatrixStack ms, VertexConsumerProvider vcp,
                               int light, int overlay, Vec3d cam, CallbackInfo ci) {
        var s = be.getCachedState();
        OLDWAYS$OUR.set(s.isOf(ModBlocks.RAINBOW_BANNER) || s.isOf(ModBlocks.RAINBOW_WALL_BANNER));
    }

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("RETURN")
    )
    private void oldways$end(BannerBlockEntity be, float f, MatrixStack ms, VertexConsumerProvider vcp,
                             int light, int overlay, Vec3d cam, CallbackInfo ci) {
        OLDWAYS$OUR.remove();
    }

    // simple renderCanvas(...) (no glint/solid flags)
    @Inject(
            method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void oldways$renderCanvasA(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                              int light, int overlay, ModelPart canvas,
                                              SpriteIdentifier baseSprite, boolean isBanner,
                                              DyeColor color, BannerPatternsComponent patterns,
                                              CallbackInfo ci) {
        // trigger for our rainbow BLOCK or when the ITEM renderer is active
        boolean isRainbowBlock = Boolean.TRUE.equals(OLDWAYS$OUR.get());
        if (!(isRainbowBlock)) return;

        Identifier atlas = baseSprite.getAtlasId();
        SpriteIdentifier rainbow = new SpriteIdentifier(atlas, Identifier.of("old-ways", "entity/banner/rainbow_base"));

        // draw cloth
        canvas.render(matrices, rainbow.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid), light, overlay);

        // draw patterns (vanilla)
        int max = Math.min(16, patterns.layers().size());
        for (int i = 0; i < max; i++) {
            Layer layer = patterns.layers().get(i);
            SpriteIdentifier pat = TexturedRenderLayers.getBannerPatternTextureId(layer.pattern());
            renderLayer(matrices, vertexConsumers, light, overlay, canvas, pat, layer.color());
        }

        // skip vanilla's tinted base (the greyscale mask that was whitening)
        ci.cancel();
    }

    // overload with glint/solid flags
    @Inject(
            method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;ZZ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void oldways$renderCanvasB(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                              int light, int overlay, ModelPart canvas,
                                              SpriteIdentifier baseSprite, boolean isBanner,
                                              DyeColor color, BannerPatternsComponent patterns,
                                              boolean glint, boolean solid, CallbackInfo ci) {
        oldways$renderCanvasA(matrices, vertexConsumers, light, overlay, canvas, baseSprite, isBanner, color, patterns, ci);
    }
}
