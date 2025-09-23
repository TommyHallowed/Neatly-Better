package net.hallowed.mixin.client;

import net.hallowed.client.access.ItemEntityRenderStateAccessor;
import net.hallowed.client.config.ClientConfigManager; // updated usage
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Old item rendering for:
 *  - all non-block items, and
 *  - block items whose *item model* is effectively 2D (flat sprite).
 * Extras:
 *  - yaw-only billboard (no pitch)
 *  - tiny Z thickness to avoid glint z-fighting
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Unique private static final ThreadLocal<Boolean> olditems$flatThisCall =
            ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<Double> olditems$itemX =
            ThreadLocal.withInitial(() -> 0.0);
    @Unique private static final ThreadLocal<Double> olditems$itemZ =
            ThreadLocal.withInitial(() -> 0.0);

    // Vanilla uses ~1/16 for item-depth spacing; reuse it to detect 2D models.
    @Unique private static final float Z_2D_THRESHOLD = 0.0625F;
    // Paper-thin depth to prevent enchanted glint flicker.
    @Unique private static final float Z_EPS = 0.001F;

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void olditems$markFlat(ItemEntityRenderState state, MatrixStack matrices,
                                   VertexConsumerProvider consumers, int light, CallbackInfo ci) {
        // NEW: global toggle from client config (no .get())
        if (!ClientConfigManager.oldItemRenderingEnabled()) {
            ItemEntityRenderStateAccessor acc0 = (ItemEntityRenderStateAccessor) state;
            olditems$flatThisCall.set(false);
            olditems$itemX.set(acc0.olditems$getX());
            olditems$itemZ.set(acc0.olditems$getZ());
            return;
        }

        ItemEntityRenderStateAccessor acc = (ItemEntityRenderStateAccessor) state;
        ItemStack stack = acc.olditems$getStack();

        boolean flat = true; // safe default; corrected below
        if (stack != null) {
            final Item item = stack.getItem();
            final boolean isBlock = item instanceof BlockItem;

            // If the item's *model* is very thin along Z, treat it as a 2D sprite.
            boolean modelLooks2D = false;
            try {
                Box box = state.itemRenderState.getModelBoundingBox();
                modelLooks2D = (float) box.getLengthZ() <= Z_2D_THRESHOLD + 1.0e-6F;
            } catch (Throwable ignored) {}

            // Non-blocks => 2D; block-items => 2D only when their item model is flat.
            flat = (!isBlock) || modelLooks2D;
        }

        olditems$flatThisCall.set(flat);
        olditems$itemX.set(acc.olditems$getX());
        olditems$itemZ.set(acc.olditems$getZ());
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    private void olditems$clearFlag(ItemEntityRenderState state, MatrixStack matrices,
                                    VertexConsumerProvider consumers, int light, CallbackInfo ci) {
        olditems$flatThisCall.remove();
        olditems$itemX.remove();
        olditems$itemZ.remove();
    }

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void olditems$storeStackAndXZ(net.minecraft.entity.ItemEntity entity,
                                          ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
        ItemEntityRenderStateAccessor acc = (ItemEntityRenderStateAccessor) state;
        acc.olditems$setStack(entity.getStack());
        acc.olditems$setXZ(entity.getX(), entity.getZ());
    }

    // Replace spin with yaw-only facing to the camera.
    @ModifyArg(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/RotationAxis;rotation(F)Lorg/joml/Quaternionf;"
            ),
            index = 0
    )
    private float olditems$facePlayerYawOnly(float originalRadians) {
        if (!olditems$flatThisCall.get()) return originalRadians;

        var cam = MinecraftClient.getInstance().gameRenderer.getCamera();
        double ix = olditems$itemX.get();
        double iz = olditems$itemZ.get();
        double dx = cam.getPos().x - ix;
        double dz = cam.getPos().z - iz;
        float yawToCam = (float) Math.atan2(dz, dx);
        return (float) (Math.PI * 0.5 - yawToCam);
    }

    // Swap vanilla renderStack with our flat draw when needed.
    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/ItemEntityRenderer;renderStack(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/ItemStackEntityRenderState;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/Box;)V"
            )
    )
    private void olditems$renderFlatOrVanilla(MatrixStack matrices,
                                              VertexConsumerProvider consumers,
                                              int light,
                                              ItemStackEntityRenderState state,
                                              Random random,
                                              Box box) {
        if (!olditems$flatThisCall.get()) {
            ItemEntityRenderer.renderStack(matrices, consumers, light, state, random, box);
            return;
        }

        final int copies = state.renderedAmount;
        if (copies <= 0) return;

        random.setSeed(state.seed);
        final float depth = (float) box.getLengthZ();

        if (depth > Z_2D_THRESHOLD) {
            drawOneFlat(state, matrices, consumers, light);
            for (int j = 1; j < copies; j++) {
                matrices.push();
                float dx = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                float dy = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                float dz = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                matrices.translate(dx, dy, dz);
                drawOneFlat(state, matrices, consumers, light);
                matrices.pop();
            }
        } else {
            float step = depth * 1.5F;
            matrices.translate(0.0F, 0.0F, -(step * (copies - 1) / 2.0F));
            drawOneFlat(state, matrices, consumers, light);
            matrices.translate(0.0F, 0.0F, step);
            for (int m = 1; m < copies; m++) {
                matrices.push();
                float dy = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                float dz = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                matrices.translate(dy, dz, 0.0F);
                drawOneFlat(state, matrices, consumers, light);
                matrices.pop();
                matrices.translate(0.0F, 0.0F, step);
            }
        }
    }

    @Unique
    private static void drawOneFlat(ItemStackEntityRenderState state,
                                    MatrixStack matrices,
                                    VertexConsumerProvider consumers,
                                    int light) {
        matrices.push();
        // Paper-thin to avoid z-fighting with enchanted glint.
        matrices.scale(1.0F, 1.0F, Z_EPS);
        state.itemRenderState.render(matrices, consumers, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
