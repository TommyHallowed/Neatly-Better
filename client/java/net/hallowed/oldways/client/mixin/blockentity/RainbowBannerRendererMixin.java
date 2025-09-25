package net.hallowed.oldways.client.mixin.blockentity;

import net.hallowed.oldways.client.rainbow.RainbowTicker;   // <— shared clock & color math
import net.hallowed.oldways.content.ModBlocks;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Tints the vanilla banner cloth base using the same *client animation tick* the texture
 * atlas uses (.mcmeta). With RainbowTicker this stays in perfect sync with your animated wool
 * and with the inventory item color provider.
 */
@Mixin(BannerBlockEntityRenderer.class)
public abstract class RainbowBannerRendererMixin {

    @Unique private static final ThreadLocal<Boolean> OW_IS_OURS   = ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<Boolean> OW_TINT_NEXT  = ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<Integer> OW_ARGB       = ThreadLocal.withInitial(() -> 0xFFFFFFFF);

    /* enter/exit – your client has the 7-arg render(..., Vec3d) */
    @Inject(
            method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD")
    )
    private void ow$enter(BannerBlockEntity be, float tickDelta, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, Vec3d cam, CallbackInfo ci) {
        boolean ours = be.getCachedState().isOf(ModBlocks.RAINBOW_BANNER) || be.getCachedState().isOf(ModBlocks.RAINBOW_WALL_BANNER);
        OW_IS_OURS.set(ours);
        if (ours) {
            // Same ARGB the item uses (RainbowTicker drives from client animation ticks)
            OW_ARGB.set(RainbowTicker.computeArgb());
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
            at = @At("RETURN")
    )
    private void ow$exit(BannerBlockEntity be, float td, MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay, Vec3d cam, CallbackInfo ci) {
        OW_IS_OURS.remove();
        OW_TINT_NEXT.remove();
        OW_ARGB.remove();
    }

    /* mark base pass (both overloads exist on 1.21.x) */
    @Inject(
            method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;ZZ)V",
            at = @At("HEAD"),
            require = 0
    )
    private static void ow$markBaseA(MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay,
                                     ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner,
                                     DyeColor base, BannerPatternsComponent patterns, boolean glint, boolean solid,
                                     CallbackInfo ci) {
        OW_TINT_NEXT.set(OW_IS_OURS.get() && isBanner);
    }

    @Inject(
            method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V",
            at = @At("HEAD"),
            require = 0
    )
    private static void ow$markBaseB(MatrixStack ms, VertexConsumerProvider vcp, int light, int overlay,
                                     ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner,
                                     DyeColor base, BannerPatternsComponent patterns, CallbackInfo ci) {
        OW_TINT_NEXT.set(OW_IS_OURS.get() && isBanner);
    }

    /* replace color int at ModelPart#render(..., color) for base only */
    @ModifyArgs(
            method = "renderLayer(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;Lnet/minecraft/util/DyeColor;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"
            ),
            require = 0
    )
    private static void ow$applyTint(Args args) {
        if (!OW_TINT_NEXT.get()) return;
        OW_TINT_NEXT.set(false);             // consume for this pass only
        args.set(4, OW_ARGB.get());          // arg[4] = ARGB color
    }
}
