package net.hallowed.oldways.client.feature.stuckprojectile;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import net.hallowed.oldways.client.mixin.accessor.ModelPartAccessor;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ArrowEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.ArrowEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.StingerModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public abstract class StuckProjectileFeature<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends FeatureRenderer<S, M> {

    private final Model<? super S> model;
    private final Identifier texture;
    private final StuckObjectsFeatureRenderer.RenderPosition renderPosition;

    protected StuckProjectileFeature(
            FeatureRendererContext<S, M> parent,
            Model<? super S> model,
            Identifier texture,
            StuckObjectsFeatureRenderer.RenderPosition pos
    ) {
        super(parent);
        this.model = model;
        this.texture = texture;
        this.renderPosition = pos;
    }

    protected abstract int getCount();

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                       S state, float limbAngle, float limbDistance) {
        int count = getCount();
        if (count <= 0 || state.invisible) return;

        RandomGenerator rng = java.util.random.RandomGeneratorFactory
                .of("L64X128MixRandom")
                .create(StuckProjectilesState.ENTITY_ID);

        for (int i = 0; i < count; i++) {
            matrices.push();

            Pair<ModelPart, List<ModelPart>> partAndPath =
                    pickRandomRenderablePartPath(getContextModel().getRootPart(), rng);
            if (partAndPath == null) { matrices.pop(); return; }

            for (ModelPart p : partAndPath.getSecond()) p.applyTransform(matrices);

            ModelPart part = partAndPath.getFirst();

            ModelPart.Cuboid cuboid = safeGetRandomCuboid(part, StuckProjectilesState.ENTITY_ID ^ i);
            if (cuboid == null) { matrices.pop(); continue; }

            float h = nextFloat(rng);
            float l = nextFloat(rng);
            float m = nextFloat(rng);
            if (this.renderPosition == StuckObjectsFeatureRenderer.RenderPosition.ON_SURFACE) {
                int face = (int) (nextFloat(rng) * 3.0f);
                if (face == 0) h = snapToFace(h);
                else if (face == 1) l = snapToFace(l);
                else m = snapToFace(m);
            }

            matrices.translate(
                    MathHelper.lerp(h, cuboid.minX, cuboid.maxX) / 16.0F,
                    MathHelper.lerp(l, cuboid.minY, cuboid.maxY) / 16.0F,
                    MathHelper.lerp(m, cuboid.minZ, cuboid.maxZ) / 16.0F
            );

            float dirX = -(h * 2.0F - 1.0F);
            float dirY = -(l * 2.0F - 1.0F);
            float dirZ = -(m * 2.0F - 1.0F);
            float planar = MathHelper.sqrt(dirX * dirX + dirZ * dirZ);
            float yaw = (float)(Math.atan2(dirX, dirZ) * 180.0F / Math.PI);
            float pitch = (float)(Math.atan2(dirY, planar) * 180.0F / Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw - 90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(pitch));

            FeatureRenderer.renderModel(this.model, this.texture, matrices, queue, light, state, 0xFFFFFFFF, 1);

            matrices.pop();
        }
    }

    /* ---------------- helpers ---------------- */

    private static float snapToFace(float f) { return f > 0.5F ? 1.0F : 0.5F; }
    private static float nextFloat(RandomGenerator rng) { return (rng.nextLong() >>> 11) * 0x1.0p-53f; }

    private static ModelPart.Cuboid safeGetRandomCuboid(ModelPart part, long seed) {
        try {
            return part.getRandomCuboid(net.minecraft.util.math.random.Random.create(seed));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Pair<ModelPart, List<ModelPart>> pickRandomRenderablePartPath(ModelPart root, RandomGenerator rng) {
        var out = new ArrayList<Pair<ModelPart, List<ModelPart>>>();
        collect(root, new ArrayList<>(), out);
        if (out.isEmpty()) return null;
        int idx = Math.floorMod((int) rng.nextLong(), out.size());
        return out.get(idx);
    }

    private static void collect(ModelPart cur, List<ModelPart> path, List<Pair<ModelPart, List<ModelPart>>> out) {
        List<ModelPart> newPath = new ArrayList<>(path);
        newPath.add(cur);

        if (safeGetRandomCuboid(cur, 1L) != null) {
            out.add(Pair.of(cur, newPath));
        }

        Map<String, ModelPart> children = ((ModelPartAccessor)(Object)cur).getChildren();
        if (children != null && !children.isEmpty()) {
            for (ModelPart child : children.values()) collect(child, newPath, out);
        }
    }

    /* ===== concrete features ===== */

    public static final class Arrows<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
            extends StuckProjectileFeature<S, M> {
        @SuppressWarnings("unchecked")
        public Arrows(FeatureRendererContext<S, M> parent, EntityRendererFactory.Context bake) {
            super(
                    parent,
                    (Model<? super S>)(Model<?>) new ArrowEntityModel(bake.getPart(EntityModelLayers.ARROW)),
                    ArrowEntityRenderer.TEXTURE,
                    StuckObjectsFeatureRenderer.RenderPosition.IN_CUBE
            );
        }
        @Override protected int getCount() { return StuckProjectilesState.ARROW_COUNT; }
    }

    public static final class Stingers<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
            extends StuckProjectileFeature<S, M> {
        @SuppressWarnings("unchecked")
        public Stingers(FeatureRendererContext<S, M> parent, EntityRendererFactory.Context bake) {
            super(
                    parent,
                    (Model<? super S>)(Model<?>) new StingerModel(bake.getPart(EntityModelLayers.BEE_STINGER)),
                    Identifier.ofVanilla("textures/entity/bee/bee_stinger.png"),
                    StuckObjectsFeatureRenderer.RenderPosition.ON_SURFACE
            );
        }
        @Override protected int getCount() { return StuckProjectilesState.STINGER_COUNT; }
    }
}
