package net.hallowed.oldways.client.mixin;

import net.hallowed.oldways.client.util.RainbowCycle;
import net.hallowed.oldways.client.util.RainbowRenderState;
import net.hallowed.oldways.content.RainbowSet;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerBlockEntityRenderer.class)
public abstract class RainbowBannerRendererMixin {

    // Placed banners
    @Inject(method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD"))
    private void oldways$beginWorld(BannerBlockEntity be, float tickDelta, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, net.minecraft.util.math.Vec3d cam, CallbackInfo ci) {
        var s = be.getCachedState();
        boolean ours = s.isOf(RainbowSet.RAINBOW_BANNER) || s.isOf(RainbowSet.RAINBOW_WALL_BANNER);
        if (!ours || be.getWorld() == null) { RainbowRenderState.begin(false, 0, 0); return; }
        long time = be.getWorld().getTime();
        BlockPos p = be.getPos();
        int seed = Math.abs(p.getX()*7 + p.getY()*9 + p.getZ()*13);
        int argb = RainbowCycle.argbFromAge(time + tickDelta, seed);
        RainbowRenderState.begin(true, argb, seed);
    }
    @Inject(method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("RETURN"))
    private void oldways$endWorld(BannerBlockEntity be, float f, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, net.minecraft.util.math.Vec3d cam, CallbackInfo ci) {
        RainbowRenderState.end();
    }

    // Item banners (animate when they have your rainbow pattern)
    @Inject(method = "renderAsItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V",
            at = @At("HEAD"))
    private void oldways$beginItem(MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, DyeColor base, BannerPatternsComponent patterns, CallbackInfo ci) {
        boolean hasOurPattern = patterns.layers().stream().anyMatch(this::isOurPattern);
        if (!hasOurPattern) { RainbowRenderState.begin(false, 0, 0); return; }
        long t = MinecraftClient.getInstance().world != null ? MinecraftClient.getInstance().world.getTime() : System.currentTimeMillis()/50L;
        int argb = RainbowCycle.argbFromAge(t, 0);
        RainbowRenderState.begin(true, argb, 0);
    }
    @Inject(method = "renderAsItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V",
            at = @At("RETURN"))
    private void oldways$endItem(MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, DyeColor base, BannerPatternsComponent patterns, CallbackInfo ci) {
        RainbowRenderState.end();
    }

    @Unique
    private boolean isOurPattern(BannerPatternsComponent.Layer layer) {
        // Works in 1.21.x: returns a namespaced id like "old-ways:rainbow"
        String id = layer.pattern().getIdAsString();
        return "old-ways:rainbow".equals(id);
    }

    // Redirect base layer draw to inject our ARGB (only when enabled)
    @Redirect(
            method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;ZZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BannerBlockEntityRenderer;renderLayer(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;Lnet/minecraft/util/DyeColor;)V")
    )
    private static void oldways$tintBase(MatrixStack m, VertexConsumerProvider v, int light, int overlay, ModelPart canvas, SpriteIdentifier tex, DyeColor vanilla) {
        if (RainbowRenderState.ENABLED.get() && tex.equals(TexturedRenderLayers.BANNER_BASE)) {
            int argb = RainbowRenderState.ARGB.get();
            canvas.render(m, tex.getVertexConsumer(v, RenderLayer::getEntityNoOutline), light, overlay, argb);
            return;
        }
        // vanilla fallback
        int i = vanilla.getEntityColor();
        canvas.render(m, tex.getVertexConsumer(v, RenderLayer::getEntityNoOutline), light, overlay, i);
    }
}
