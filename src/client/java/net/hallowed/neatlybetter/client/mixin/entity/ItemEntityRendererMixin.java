package net.hallowed.neatlybetter.client.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Unique
    private static final float PAPER_Z_SCALE = 0.05f;

    @Unique
    private static final float PAPER_STACK_SPACING = 0.025f;

    @Unique
    private static final float PAPER_STACK_JITTER = 0.075f;

    @Unique
    private static final ThreadLocal<Boolean> neatlybetter$isFlat =
            ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<double[]> neatlybetter$entityPos =
            ThreadLocal.withInitial(() -> new double[2]);

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void neatlybetter$detectFlatItem(
            ItemEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {

        if (!NTClientConfig.CONFIG.render2DItems.get()
                || state.item.isEmpty()
                || state.item.usesBlockLight()) {
            neatlybetter$isFlat.set(false);
            return;
        }

        neatlybetter$isFlat.set(true);
        double[] pos = neatlybetter$entityPos.get();
        pos[0] = state.x;
        pos[1] = state.z;
    }

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/math/Axis;rotation(F)Lorg/joml/Quaternionf;"),
            index = 0
    )
    private float neatlybetter$billboardRotation(float spinAngle) {
        if (!neatlybetter$isFlat.get()) return spinAngle;

        double[] pos = neatlybetter$entityPos.get();
        var cam = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        return (float) Math.atan2(cam.x - pos[0], cam.z - pos[1]);
    }

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;"
                            + "submitMultipleFromCount("
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;"
                            + "Lnet/minecraft/util/RandomSource;"
                            + "Lnet/minecraft/world/phys/AABB;)V")
    )
    private void neatlybetter$submitFlat(
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            ItemClusterRenderState state, RandomSource random, AABB modelBoundingBox,
            Operation<Void> original) {

        if (!neatlybetter$isFlat.get()) {
            original.call(poseStack, submitNodeCollector, lightCoords, state, random, modelBoundingBox);
            return;
        }

        int count = state.count;
        if (count == 0) return;

        random.setSeed(state.seed);

        poseStack.translate(0.0f, 0.0f, -(PAPER_STACK_SPACING * (float) (count - 1) / 2.0f));

        poseStack.pushPose();
        poseStack.scale(1.0f, 1.0f, PAPER_Z_SCALE);
        state.item.submit(poseStack, submitNodeCollector, lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        poseStack.translate(0.0f, 0.0f, PAPER_STACK_SPACING);

        for (int i = 1; i < count; i++) {
            poseStack.pushPose();
            float jitterX = (random.nextFloat() * 2.0f - 1.0f) * PAPER_STACK_JITTER;
            float jitterY = (random.nextFloat() * 2.0f - 1.0f) * PAPER_STACK_JITTER;
            poseStack.translate(jitterX, jitterY, 0.0f);
            poseStack.scale(1.0f, 1.0f, PAPER_Z_SCALE);
            state.item.submit(poseStack, submitNodeCollector, lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
            poseStack.translate(0.0f, 0.0f, PAPER_STACK_SPACING);
        }
    }
}