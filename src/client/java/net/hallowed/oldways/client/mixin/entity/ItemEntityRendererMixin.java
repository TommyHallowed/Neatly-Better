package net.hallowed.oldways.client.mixin.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.hallowed.oldways.client.util.ItemEntityRenderStateAccessor;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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

    @Unique private static final ThreadLocal<Boolean> olditems$flatThisCall =
            ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<Double> olditems$itemX =
            ThreadLocal.withInitial(() -> 0.0);
    @Unique private static final ThreadLocal<Double> olditems$itemZ =
            ThreadLocal.withInitial(() -> 0.0);

    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Unique private static final float Z_2D_THRESHOLD = 0.0625F;
    @Unique private static final float Z_EPS = 0.001F;


    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void olditems$markFlat(ItemEntityRenderState state, PoseStack matrices,
                                   SubmitNodeCollector queue, CameraRenderState camera,
                                   CallbackInfo ci) {
        ItemEntityRenderStateAccessor acc0 = (ItemEntityRenderStateAccessor) state;
        if (!OW$prefs.render2DItems) {
            olditems$flatThisCall.set(false);
            olditems$itemX.set(acc0.olditems$getX());
            olditems$itemZ.set(acc0.olditems$getZ());
            return;
        }

        ItemStack stack = acc0.olditems$getStack();
        boolean flat = isFlat(state, stack);
        olditems$flatThisCall.set(flat);
        olditems$itemX.set(acc0.olditems$getX());
        olditems$itemZ.set(acc0.olditems$getZ());
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void olditems$clearFlag(ItemEntityRenderState state, PoseStack matrices,
                                    SubmitNodeCollector queue, CameraRenderState camera,
                                    CallbackInfo ci) {
        olditems$flatThisCall.remove();
        olditems$itemX.remove();
        olditems$itemZ.remove();
    }


    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void olditems$storeStackAndXZ(net.minecraft.world.entity.item.ItemEntity entity,
                                          ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
        ItemEntityRenderStateAccessor acc = (ItemEntityRenderStateAccessor) state;
        acc.olditems$setStack(entity.getItem());
        acc.olditems$setXZ(entity.getX(), entity.getZ());
    }


    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/math/Axis;rotation(F)Lorg/joml/Quaternionf;"
            ),
            index = 0
    )
    private float olditems$facePlayerYawOnly(float originalRadians) {
        if (!olditems$flatThisCall.get()) return originalRadians;

        var cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        double ix = olditems$itemX.get();
        double iz = olditems$itemZ.get();
        double dx = cam.position().x - ix;
        double dz = cam.position().z - iz;
        float yawToCam = (float) Math.atan2(dz, dx);
        return (float) (Math.PI * 0.5 - yawToCam);
    }


    @Redirect(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V"
            )
    )
    private void olditems$renderFlatOrVanilla(
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            ItemClusterRenderState state,
            RandomSource random,
            AABB boundingBox
    ) {
        if (!olditems$flatThisCall.get()) {
            ItemEntityRenderer.renderMultipleFromCount(matrices, queue, light, state, random);
            return;
        }

        final int copies = state.count;
        if (copies <= 0) return;

        random.setSeed(state.seed);
        final AABB box = state.item.getModelBoundingBox();
        final float depth = (float) box.getZsize();

        if (depth > Z_2D_THRESHOLD) {
            drawOneFlat(state, matrices, queue, light);
            for (int j = 1; j < copies; j++) {
                matrices.pushPose();
                float dx = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                float dy = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                float dz = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                matrices.translate(dx, dy, dz);
                drawOneFlat(state, matrices, queue, light);
                matrices.popPose();
            }
        } else {
            float step = depth * 1.5F;
            matrices.translate(0.0F, 0.0F, -(step * (copies - 1) / 2.0F));
            drawOneFlat(state, matrices, queue, light);
            matrices.translate(0.0F, 0.0F, step);
            for (int m = 1; m < copies; m++) {
                matrices.pushPose();
                float dy = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                float dz = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                matrices.translate(dy, dz, 0.0F);
                drawOneFlat(state, matrices, queue, light);
                matrices.popPose();
                matrices.translate(0.0F, 0.0F, step);
            }
        }
    }

    @Unique
    private static boolean isFlat(ItemEntityRenderState state, ItemStack stack) {
        if (stack == null) return false;

        final Item item = stack.getItem();
        final boolean isBlock = item instanceof BlockItem;

        boolean modelLooks2D = false;
        try {
            AABB box = state.item.getModelBoundingBox();
            modelLooks2D = (float) box.getZsize() <= Z_2D_THRESHOLD + 1.0e-6F;
        } catch (Throwable ignored) {}

        return (!isBlock) && modelLooks2D;
    }

    @Unique
    private static void drawOneFlat(ItemClusterRenderState state,
                                    PoseStack matrices,
                                    SubmitNodeCollector queue,
                                    int light) {
        matrices.pushPose();
        matrices.scale(1.0F, 1.0F, Z_EPS);
        state.item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrices.popPose();
    }
}
