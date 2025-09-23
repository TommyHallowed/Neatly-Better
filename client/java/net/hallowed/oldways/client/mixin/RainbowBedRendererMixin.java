package net.hallowed.oldways.client.mixin;

import net.hallowed.oldways.content.ModBlocks;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BedBlockEntityRenderer;
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

@Mixin(BedBlockEntityRenderer.class)
public abstract class RainbowBedRendererMixin {

    @Unique private static final ThreadLocal<Boolean> OLDWAYS$OUR_BED = ThreadLocal.withInitial(() -> false);
    @Unique private static final SpriteIdentifier OLDWAYS$BED_SPRITE =
            new SpriteIdentifier(
                    net.minecraft.client.render.TexturedRenderLayers.BEDS_ATLAS_TEXTURE,
                    Identifier.of("old-ways", "entity/bed/rainbow")
            );

    // tag if the current BE is our custom bed
    @Inject(
            method = "render(Lnet/minecraft/block/entity/BedBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD")
    )
    private void oldways$begin(BedBlockEntity be, float tickDelta, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, Vec3d cam, CallbackInfo ci) {
        OLDWAYS$OUR_BED.set(be.getCachedState().isOf(ModBlocks.RAINBOW_BED));
    }

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BedBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("RETURN")
    )
    private void oldways$end(BedBlockEntity be, float tickDelta, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, Vec3d cam, CallbackInfo ci) {
        OLDWAYS$OUR_BED.remove();
    }

    /**
     * BedBlockEntityRenderer#renderPart(...) calls:
     *   sprite.getVertexConsumer(provider, RenderLayer::getEntitySolid)
     * Redirect that 2-arg overload and swap sprite when ours.
     */
    @Redirect(
            method = "renderPart(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/model/Model;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/util/SpriteIdentifier;IIZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;")
    )
    private VertexConsumer oldways$swapBedTexture(SpriteIdentifier original,
                                                  VertexConsumerProvider provider,
                                                  Function<Identifier, RenderLayer> layerFactory) {
        if (OLDWAYS$OUR_BED.get()) {
            return OLDWAYS$BED_SPRITE.getVertexConsumer(provider, layerFactory);
        }
        return original.getVertexConsumer(provider, layerFactory);
    }
}
