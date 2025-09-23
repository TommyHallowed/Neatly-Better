package net.hallowed.oldways.client.mixin;

import net.hallowed.oldways.client.util.RainbowCycle;
import net.hallowed.oldways.client.util.RainbowRenderState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BedBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedBlockEntityRenderer.class)
public abstract class RainbowBedRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BedBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD")
    )
    private void oldways$begin(BedBlockEntity be, float tickDelta, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, net.minecraft.util.math.Vec3d cam, CallbackInfo ci) {
        var s = be.getCachedState();
        boolean ours = oldways$isBlockId(s, "rainbow_bed");
        if (!ours || be.getWorld() == null) { RainbowRenderState.begin(false, 0, 0); return; }
        long t = be.getWorld().getTime();
        var p = be.getPos();
        int seed = Math.abs(p.getX()*7 + p.getY()*9 + p.getZ()*13);
        int argb = RainbowCycle.argbFromAge(t + tickDelta, seed);
        RainbowRenderState.begin(true, argb, seed);
    }

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BedBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("RETURN")
    )
    private void oldways$end(BedBlockEntity be, float f, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, net.minecraft.util.math.Vec3d cam, CallbackInfo ci) {
        RainbowRenderState.end();
    }

    // Swap Model#render(...) with colored ModelPart#render(...)
    @Redirect(
            method = "renderPart(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/model/Model;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/util/SpriteIdentifier;IIZ)V",
            at    = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V")
    )
    private void oldways$colorizeBed(Model model, MatrixStack m, VertexConsumer vc, int light, int overlay) {
        // Cast to the actual concrete type created in BedBlockEntityRenderer's ctor
        ModelPart root = (model).getRootPart();
        if (RainbowRenderState.ENABLED.get()) {
            root.render(m, vc, light, overlay, RainbowRenderState.ARGB.get());
        } else {
            root.render(m, vc, light, overlay);
        }
    }

    @Unique
    private static boolean oldways$isBlockId(net.minecraft.block.BlockState state, String path) {
        return state.getBlock().getRegistryEntry().getKey()
                .map(key -> {
                    Identifier id = key.getValue();
                    return id.getNamespace().equals("old-ways") && id.getPath().equals(path);
                })
                .orElse(false);
    }
}
