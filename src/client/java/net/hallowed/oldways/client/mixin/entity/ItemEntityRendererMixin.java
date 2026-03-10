package net.hallowed.oldways.client.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Unique private static final float Z_2D_THRESHOLD = 0.0625F;
    @Unique private static final float Z_EPS = 0.001F;

    // Thread-safe: prevents issues with minimap mods or async rendering
    @Unique private static final ThreadLocal<Boolean> oldways$isFlat = ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<double[]> oldways$lastPos = ThreadLocal.withInitial(() -> new double[2]);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    private void oldways$syncStackToState(ItemEntity entity, ItemEntityRenderState state, float f, CallbackInfo ci) {
        ((ItemEntityStackHolder) state).oldways$setStack(entity.getItem());
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"))
    private void oldways$calculateFlatness(ItemEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {
        double[] pos = oldways$lastPos.get();
        pos[0] = state.x;
        pos[1] = state.z;

        if (!SettingsPrefs.get().render2DItems) {
            oldways$isFlat.set(false);
            return;
        }

        ItemStack stack = ((ItemEntityStackHolder) state).oldways$getStack();

        if (stack.isEmpty() || stack.getItem() instanceof BlockItem) {
            oldways$isFlat.set(false);
        } else {
            AABB box = state.item.getModelBoundingBox();
            oldways$isFlat.set(box.getZsize() <= Z_2D_THRESHOLD);
        }
    }

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotation(F)Lorg/joml/Quaternionf;"),
            index = 0
    )
    private float oldways$billboardYaw(float originalRadians) {
        if (!oldways$isFlat.get()) return originalRadians;

        double[] pos = oldways$lastPos.get();
        var camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        double dx = camPos.x - pos[0];
        double dz = camPos.z - pos[1];

        return (float) (Math.PI * 0.5 - Math.atan2(dz, dx));
    }

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V")
    )
    private void oldways$wrapRender(
            PoseStack matrices, SubmitNodeCollector queue, int light,
            ItemClusterRenderState state, RandomSource random, AABB box,
            Operation<Void> original
    ) {
        if (!oldways$isFlat.get()) {
            original.call(matrices, queue, light, state, random, box);
            return;
        }

        int count = state.count;
        if (count <= 0) return;

        random.setSeed(state.seed);
        float depth = (float) state.item.getModelBoundingBox().getZsize();
        float step = depth * 1.5F;

        matrices.translate(0.0F, 0.0F, -(step * (count - 1) / 2.0F));

        for (int i = 0; i < count; i++) {
            matrices.pushPose();

            if (i > 0) {
                matrices.translate(
                        (random.nextFloat() * 2.0F - 1.0F) * 0.05F,
                        (random.nextFloat() * 2.0F - 1.0F) * 0.05F,
                        0.0F
                );
            }

            matrices.pushPose();
            matrices.scale(1.0F, 1.0F, Z_EPS);
            state.item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            matrices.popPose();

            matrices.popPose();
            matrices.translate(0.0F, 0.0F, step);
        }
    }
}