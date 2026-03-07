package net.hallowed.oldways.client.mixin.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.hallowed.oldways.client.util.ItemEntityStackHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Unique private static final float Z_2D_THRESHOLD = 0.0625F;
    @Unique private static final float Z_EPS = 0.001F;

    @Unique private boolean oldways$isFlat;
    @Unique private double oldways$lastX;
    @Unique private double oldways$lastZ;

    /**
     * Captures the ItemStack from the Entity and stores it in our RenderState
     * via the Duck Interface so it's available during the submit pass.
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    private void oldways$syncStackToState(ItemEntity entity, ItemEntityRenderState state, float f, CallbackInfo ci) {
        ((ItemEntityStackHolder) state).oldways$setStack(entity.getItem());
    }

    /**
     * Pre-calculates whether the item should be rendered as a 2D billboard.
     * Uses Access Wideners for state.x and state.z.
     */
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"))
    private void oldways$calculateFlatness(ItemEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {
        // x and z are public via Access Widener
        this.oldways$lastX = state.x;
        this.oldways$lastZ = state.z;

        if (!SettingsPrefs.get().render2DItems) {
            this.oldways$isFlat = false;
            return;
        }

        // Access the stack via interface to avoid Mixin direct reference errors
        ItemStack stack = ((ItemEntityStackHolder) state).oldways$getStack();

        if (stack.isEmpty() || stack.getItem() instanceof BlockItem) {
            this.oldways$isFlat = false;
        } else {
            // state.item is the ItemStackRenderState (the baked model data)
            AABB box = state.item.getModelBoundingBox();
            this.oldways$isFlat = box.getZsize() <= Z_2D_THRESHOLD;
        }
    }

    /**
     * Forces the item to face the player's Y-axis (Billboard) if it's marked as flat.
     */
    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotation(F)Lorg/joml/Quaternionf;"),
            index = 0
    )
    private float oldways$billboardYaw(float originalRadians) {
        if (!this.oldways$isFlat) return originalRadians;

        var camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        double dx = camPos.x - this.oldways$lastX;
        double dz = camPos.z - this.oldways$lastZ;

        // Face the player
        return (float) (Math.PI * 0.5 - Math.atan2(dz, dx));
    }

    /**
     * Redirects the cluster rendering to our custom 2D stacking logic.
     */
    @Redirect(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V")
    )
    private void oldways$redirectRender(PoseStack matrices, SubmitNodeCollector queue, int light, ItemClusterRenderState state, RandomSource random, AABB box) {
        if (!this.oldways$isFlat) {
            ItemEntityRenderer.submitMultipleFromCount(matrices, queue, light, state, random, box);
            return;
        }

        int count = state.count;
        if (count <= 0) return;

        random.setSeed(state.seed);
        float depth = (float) state.item.getModelBoundingBox().getZsize();
        float step = depth * 1.5F;

        // Spread flat items along the Z-axis (pointing towards player due to billboard)
        matrices.translate(0.0F, 0.0F, -(step * (count - 1) / 2.0F));

        for (int i = 0; i < count; i++) {
            matrices.pushPose();

            // Random jitter so stacks don't look perfectly robotic
            if (i > 0) {
                matrices.translate(
                        (random.nextFloat() * 2.0F - 1.0F) * 0.05F,
                        (random.nextFloat() * 2.0F - 1.0F) * 0.05F,
                        0.0F
                );
            }

            // Draw the flattened model
            matrices.pushPose();
            matrices.scale(1.0F, 1.0F, Z_EPS);
            state.item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            matrices.popPose();

            matrices.popPose();
            matrices.translate(0.0F, 0.0F, step);
        }
    }
}