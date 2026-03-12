package net.hallowed.oldways.client.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
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

    // Z scale factor — thin enough to look like paper, thick enough to avoid
    // degenerate-geometry issues (z-fighting, broken normals, GPU clipping).
    // At 0.05 the item is 1/320 of a block thick — completely invisible with
    // billboard rotation, yet 50× safer than the old 0.001 value.
    @Unique
    private static final float PAPER_Z_SCALE = 0.05f;

    // ThreadLocal keeps minimap mods and any off-thread renderers safe.
    @Unique
    private static final ThreadLocal<Boolean> oldways$isFlat =
            ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<double[]> oldways$entityPos =
            ThreadLocal.withInitial(() -> new double[2]);

    // ── 1. Flat-item detection ──────────────────────────────────────────────

    /**
     * Runs at the very start of {@code submit()}.  Decides whether the current
     * item entity is a flat sprite by checking {@code usesBlockLight()}:
     * <ul>
     *   <li>{@code false} → flat / generated model  (swords, food, ingots …)</li>
     *   <li>{@code true}  → 3D model  (blocks, trident, shield, spyglass …)</li>
     * </ul>
     * Also caches the entity's world position for the billboard calculation.
     */
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void oldways$detectFlatItem(
            ItemEntityRenderState state, PoseStack matrices,
            SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {

        if (!SettingsPrefs.get().render2DItems
                || state.item.isEmpty()
                || state.item.usesBlockLight()) {
            oldways$isFlat.set(false);
            return;
        }

        oldways$isFlat.set(true);
        double[] pos = oldways$entityPos.get();
        pos[0] = state.x;
        pos[1] = state.z;
    }

    // ── 2. Billboard rotation ───────────────────────────────────────────────

    /**
     * Replaces vanilla's spinning animation with a Y-axis billboard that always
     * faces the camera.  {@code atan2(dx, dz)} is the standard MC facing-angle
     * formula and is mathematically equivalent to
     * {@code π/2 − atan2(dz, dx)}.
     */
    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/math/Axis;rotation(F)Lorg/joml/Quaternionf;"),
            index = 0
    )
    private float oldways$billboardRotation(float spinAngle) {
        if (!oldways$isFlat.get()) return spinAngle;

        double[] pos = oldways$entityPos.get();
        var cam = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        return (float) Math.atan2(cam.x - pos[0], cam.z - pos[1]);
    }

    // ── 3. Single-sprite submission ─────────────────────────────────────────

    /**
     * For flat items, submits one sprite instead of vanilla's count-based
     * multi-copy stacking (up to 5 copies).  This is the primary performance
     * win — fewer draw calls, no random offsets, no extra matrix work.
     */
    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;"
                            + "submitMultipleFromCount("
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;"
                            + "Lnet/minecraft/util/RandomSource;"
                            + "Lnet/minecraft/world/phys/AABB;)V")
    )
    private void oldways$submitFlat(
            PoseStack matrices, SubmitNodeCollector queue, int light,
            ItemClusterRenderState cluster, RandomSource random, AABB box,
            Operation<Void> original) {

        if (!oldways$isFlat.get()) {
            original.call(matrices, queue, light, cluster, random, box);
            return;
        }

        // One sprite, paper-thin.  The Z scale is applied in local space AFTER
        // the billboard rotation, so it compresses depth toward the camera —
        // exactly the axis you'll never see edge-on.
        matrices.pushPose();
        matrices.scale(1.0f, 1.0f, PAPER_Z_SCALE);
        cluster.item.submit(matrices, queue, light,
                OverlayTexture.NO_OVERLAY, cluster.outlineColor);
        matrices.popPose();
    }
}
