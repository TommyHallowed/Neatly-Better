package net.hallowed.neatlybetter.client.feature.stuckarrows;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Arrows stuck in mobs, like vanilla draws on players.
 *
 * <p>Vanilla already counts arrows hits on every living entity ({@code LivingEntity.getArrowCount}, synced to
 * clients and removed over time); only the player renderer has a layer for it. This adds the same layer to every
 * other living entity renderer, gated by the client option {@code show_stuck_projectiles}.
 */
public final class MobStuckArrows {
    private MobStuckArrows() {}

    /** Arrow count and a per-entity seed (entity id) so arrows keep their spots between frames. */
    public record Stuck(int count, int seed) {}

    public static final RenderStateDataKey<Stuck> STUCK_ARROWS = RenderStateDataKey.create(() -> "neatly-better:stuck_arrows");

    public static void register() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer<?>) return; // players already have vanilla's ArrowLayer
            helper.register(new Layer<>(castParent(renderer), context));
        });
    }

    /** Called from the render-state extraction mixin. */
    public static void extract(LivingEntity entity, LivingEntityRenderState state) {
        int count = NTClientConfig.CONFIG.showStuckProjectiles.get() ? entity.getArrowCount() : 0;
        state.setData(STUCK_ARROWS, count > 0 ? new Stuck(count, entity.getId()) : null);
    }

    @SuppressWarnings("unchecked")
    private static <S extends LivingEntityRenderState, M extends EntityModel<S>> RenderLayerParent<S, M> castParent(
            LivingEntityRenderer<?, ?, ?> renderer) {
        return (RenderLayerParent<S, M>) renderer;
    }

    private static final class Layer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
        private final ArrowModel arrowModel;
        private final ArrowRenderState arrowState = new ArrowRenderState();

        Layer(RenderLayerParent<S, M> parent, EntityRendererProvider.Context context) {
            super(parent);
            this.arrowModel = new ArrowModel(context.bakeLayer(ModelLayers.ARROW));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
            Stuck stuck = state.getData(STUCK_ARROWS);
            if (stuck == null || state.isInvisible) return;

            List<ModelPart[]> parts = new ArrayList<>();
            collectParts(getParentModel().root(), new ArrayList<>(), parts);
            if (parts.isEmpty()) return;

            // Same placement as vanilla's StuckInBodyLayer (IN_CUBE), but walking the full part hierarchy so
            // nested parts (heads, limbs on a body) get their parents' transforms.
            RandomSource random = RandomSource.createThreadLocalInstance(stuck.seed());
            for (int i = 0; i < stuck.count(); i++) {
                ModelPart[] path = parts.get(random.nextInt(parts.size()));
                ModelPart part = path[path.length - 1];
                ModelPart.Cube cube = part.getRandomCube(random);

                poseStack.pushPose();
                for (ModelPart p : path) p.translateAndRotate(poseStack);

                float midX = random.nextFloat();
                float midY = random.nextFloat();
                float midZ = random.nextFloat();
                poseStack.translate(
                        Mth.lerp(midX, cube.minX, cube.maxX) / 16.0F,
                        Mth.lerp(midY, cube.minY, cube.maxY) / 16.0F,
                        Mth.lerp(midZ, cube.minZ, cube.maxZ) / 16.0F);

                float dirX = -(midX * 2.0F - 1.0F);
                float dirY = -(midY * 2.0F - 1.0F);
                float dirZ = -(midZ * 2.0F - 1.0F);
                float dirXZ = Mth.sqrt(dirX * dirX + dirZ * dirZ);
                poseStack.rotateDegrees(Axis.YP, (float) (Math.atan2(dirX, dirZ) * 180.0F / Math.PI) - 90.0F);
                poseStack.rotateDegrees(Axis.ZP, (float) (Math.atan2(dirY, dirXZ) * 180.0F / Math.PI));

                collector.submitModel(arrowModel, arrowState, poseStack, TippableArrowRenderer.NORMAL_ARROW_LOCATION,
                        lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }

        /** Visible parts that have cubes, each with its path from the root (in model order, so it's stable). */
        private static void collectParts(ModelPart part, List<ModelPart> path, List<ModelPart[]> out) {
            if (!part.visible) return;
            path.add(part);
            if (!part.isEmpty() && !part.skipDraw) out.add(path.toArray(ModelPart[]::new));
            for (ModelPart child : part.children.values()) collectParts(child, path, out);
            path.removeLast();
        }
    }
}
