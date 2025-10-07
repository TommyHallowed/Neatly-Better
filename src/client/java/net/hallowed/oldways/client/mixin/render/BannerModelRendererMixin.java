// src/client/java/net/hallowed/oldways/client/mixin/render/BannerModelRendererMixin.java
package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.util.BannerSwapState;
import net.hallowed.oldways.init.ModBlocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.model.special.BannerModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * (ModifiedClass)Mixin
 * Scope the item swap to *our* rainbow banner items only, and make the flag
 * lifetime exactly one render() call to avoid affecting other banners.
 */
@Mixin(BannerModelRenderer.class)
public abstract class BannerModelRendererMixin {

    /** Decide if the *next* banner render should use our base. */
    @Inject(
            method = "getData(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/component/type/BannerPatternsComponent;",
            at = @At("HEAD")
    )
    private void oldways$markPendingIfRainbow(ItemStack stack,
                                              CallbackInfoReturnable<BannerPatternsComponent> cir) {
        boolean isRainbow = stack != null && !stack.isEmpty()
                && (stack.isOf(ModBlocks.RAINBOW_BANNER.asItem())
                || stack.isOf(ModBlocks.RAINBOW_WALL_BANNER.asItem()));
        BannerSwapState.PENDING_ITEM.set(isRainbow);
    }

    /** Consume the pending flag at the start of render() and clear it immediately. */
    @Inject(
            method = "render(Lnet/minecraft/component/type/BannerPatternsComponent;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V",
            at = @At("HEAD")
    )
    private void oldways$beginItem(@Nullable BannerPatternsComponent data,
                                   ItemDisplayContext ctx,
                                   MatrixStack matrices,
                                   VertexConsumerProvider providers,
                                   int light, int overlay, boolean glint,
                                   CallbackInfo ci) {
        if (Boolean.TRUE.equals(BannerSwapState.PENDING_ITEM.get())) {
            BannerSwapState.OUR_ITEM.set(true);   // only *this* banner render
        } else {
            BannerSwapState.OUR_ITEM.remove();    // ensure no stale state
        }
        BannerSwapState.PENDING_ITEM.remove();    // never leak to the next draw
    }

    /** Always clear after the item render completes. */
    @Inject(
            method = "render(Lnet/minecraft/component/type/BannerPatternsComponent;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V",
            at = @At("RETURN")
    )
    private void oldways$endItem(@Nullable BannerPatternsComponent data,
                                 ItemDisplayContext ctx,
                                 MatrixStack matrices,
                                 VertexConsumerProvider providers,
                                 int light, int overlay, boolean glint,
                                 CallbackInfo ci) {
        BannerSwapState.OUR_ITEM.remove();
    }
}
