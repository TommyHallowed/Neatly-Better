package net.hallowed.oldways.client.mixin.blockentity;

import net.hallowed.oldways.client.util.BannerSwapState;
import net.hallowed.oldways.init.ModBlocks;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/**
 * (ModifiedClass)Mixin
 * Swap ONLY the banner base (TexturedRenderLayers.BANNER_BASE) when our rainbow
 * banner (standing or wall) is being rendered. Patterns, wave, lighting, etc. remain vanilla.
 * Works for:
 *  - world banners (this mixin sets OUR_BLOCK)
 *  - items and loom preview (other mixins set OUR_ITEM)
 */
@Mixin(BannerBlockEntityRenderer.class)
public abstract class BannerBlockEntityRendererMixin {

    /** Our sprite path (must exist at: assets/old-ways/textures/entity/banner/rainbow_base.png). */
    @Unique
    private static final Identifier OLDWAYS_BANNER_BASE_TEX =
            Identifier.of("old-ways", "entity/banner/rainbow_base");

    /* ----- World BE render: set/clear the "our block" flag ----- */

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD")
    )
    private void oldways$begin(BannerBlockEntity be, float tickDelta, MatrixStack matrices,
                               VertexConsumerProvider providers, int light, int overlay, Vec3d camPos,
                               CallbackInfo ci) {
        var state = be.getCachedState();
        if (state != null && (state.isOf(ModBlocks.RAINBOW_BANNER) || state.isOf(ModBlocks.RAINBOW_WALL_BANNER))) {
            BannerSwapState.OUR_BLOCK.set(true);
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("RETURN")
    )
    private void oldways$end(BannerBlockEntity be, float tickDelta, MatrixStack matrices,
                             VertexConsumerProvider providers, int light, int overlay, Vec3d camPos,
                             CallbackInfo ci) {
        BannerSwapState.OUR_BLOCK.remove();
    }

    /* ----- Swap only the base layer; reuse vanilla's atlas id from the callsite ----- */

    @Redirect(
            method = "renderLayer(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;Lnet/minecraft/util/DyeColor;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;")
    )
    private static VertexConsumer oldways$swapBase(SpriteIdentifier textureId,
                                                   VertexConsumerProvider providers,
                                                   Function<Identifier, RenderLayer> layerFactory) {
        // Only active during our block render or our item/loom render (flags set elsewhere),
        // and only when vanilla is requesting the base layer.
        boolean active = Boolean.TRUE.equals(BannerSwapState.OUR_BLOCK.get())
                || Boolean.TRUE.equals(BannerSwapState.OUR_ITEM.get());
        if (active && textureId.equals(TexturedRenderLayers.BANNER_BASE)) {
            // SAME atlas as vanilla asked for; just point to our sprite path
            SpriteIdentifier ours = new SpriteIdentifier(textureId.getAtlasId(), OLDWAYS_BANNER_BASE_TEX);
            return ours.getVertexConsumer(providers, layerFactory);
        }
        return textureId.getVertexConsumer(providers, layerFactory);
    }
}
