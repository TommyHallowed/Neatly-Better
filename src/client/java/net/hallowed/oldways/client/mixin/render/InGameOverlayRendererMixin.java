package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.util.FlameOverlayState;
import net.hallowed.oldways.util.FireSourceHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InGameOverlayRenderer.class)
abstract class InGameOverlayRendererMixin {
    @ModifyVariable(
            method = "renderFireOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private static Sprite oldways$swapOverlay(Sprite original) {
        var player = MinecraftClient.getInstance().player;
        if (player instanceof FireSourceHolder) {
            return FlameOverlayState.sprite1(player);
        }
        return original;
    }
}
