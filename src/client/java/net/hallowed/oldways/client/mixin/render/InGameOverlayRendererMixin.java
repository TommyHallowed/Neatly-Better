package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.util.FlameOverlayState;
import net.hallowed.oldways.util.FireSourceHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameOverlayRenderer.class)
abstract class InGameOverlayRendererMixin {

    @ModifyArg(
            method = "renderOverlays(ZFLnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameOverlayRenderer;" +
                            "renderFireOverlay(Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/VertexConsumerProvider;" +
                            "Lnet/minecraft/client/texture/Sprite;)V"
            ),
            index = 2
    )
    private Sprite oldways$swapFireOverlay(Sprite original) {
        var player = MinecraftClient.getInstance().player;
        if (player instanceof FireSourceHolder) {
            return FlameOverlayState.sprite1(player);
        }
        return original;
    }
}
