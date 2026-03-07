package net.hallowed.oldways.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.hallowed.oldways.api.LinkableMinecart;
import net.hallowed.oldways.client.util.ChainableRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartRenderer.class)
public abstract class AbstractMinecartEntityRendererMixin<T extends AbstractMinecart, S extends MinecartRenderState> {

    @Shadow protected abstract void submitMinecartContents(S state, BlockState blockState, PoseStack matrices, SubmitNodeCollector queue, int light);

    @Unique
    private Vec3 oldways$getVisualPos(AbstractMinecart cart, float tickDelta) {
        Vec3 rawPos = cart.getPosition(tickDelta);
        if (cart.getBehavior() instanceof OldMinecartBehavior def) {
            Vec3 present = def.getPos(rawPos.x, rawPos.y, rawPos.z);
            if (present != null) {
                Vec3 future = def.getPosOffs(rawPos.x, rawPos.y, rawPos.z, 0.3);
                Vec3 past = def.getPosOffs(rawPos.x, rawPos.y, rawPos.z, -0.3);
                double y = ((future != null ? future.y : present.y) + (past != null ? past.y : present.y)) / 2.0;
                return new Vec3(present.x, y, present.z);
            }
        }
        return rawPos;
    }

    // FIX 1: getBoundingBox -> getBoundingBoxForCulling
    @Inject(method = "getBoundingBoxForCulling*", at = @At("RETURN"), cancellable = true)
    private void oldways$expandRenderBox(T entity, CallbackInfoReturnable<AABB> cir) {
        int linkedId = ((LinkableMinecart) entity).oldways$getFollowingId();
        if (linkedId != -1) {
            Entity e = entity.level().getEntity(linkedId);
            if (e instanceof AbstractMinecart linked) {
                cir.setReturnValue(cir.getReturnValue().minmax(linked.getBoundingBox()));
            }
        }
    }

    // FIX 2: updateRenderState* -> extractRenderState
    @Inject(method = "extractRenderState*", at = @At("TAIL"))
    private void oldways$updateChainState(T entity, S state, float tickDelta, CallbackInfo ci) {
        int linkedId = ((LinkableMinecart) entity).oldways$getFollowingId();
        AbstractMinecart linked = null;

        if (linkedId != -1) {
            Entity e = entity.level().getEntity(linkedId);
            if (e instanceof AbstractMinecart targetCart) {
                linked = targetCart;
            }
        }

        if (linked != null) {
            Vec3 visualMyPos = oldways$getVisualPos(entity, tickDelta);
            Vec3 visualTargetPos = oldways$getVisualPos(linked, tickDelta);

            Vec3 rawPos = new Vec3(state.x, state.y, state.z);
            ((ChainableRenderState) state).oldways$setVisualOffset(visualMyPos.subtract(rawPos));
            ((ChainableRenderState) state).oldways$setLinkedPos(visualTargetPos.subtract(visualMyPos));

        } else {
            ((ChainableRenderState) state).oldways$setLinkedPos(null);
        }
    }

    // FIX 3: render* -> submit
    @Inject(method = "submit*", at = @At("TAIL"))
    private void oldways$renderChainVisuals(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        Vec3 offset = ((ChainableRenderState) state).oldways$getLinkedPos();
        if (offset == null) return;

        Vec3 visualOffset = ((ChainableRenderState) state).oldways$getVisualOffset();

        matrices.pushPose();
        matrices.translate(visualOffset.x, visualOffset.y + 0.375, visualOffset.z);

        double dx = offset.x;
        double dy = offset.y;
        double dz = offset.z;

        float yaw = (float) (Mth.atan2(dx, dz) * 180.0F / Math.PI);
        float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0F / Math.PI);

        matrices.mulPose(Axis.YP.rotationDegrees(yaw));
        matrices.mulPose(Axis.XP.rotationDegrees(pitch));

        // FIX 4: Blocks.IRON_CHAIN -> Blocks.CHAIN
        BlockState chainState = Blocks.IRON_CHAIN.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);

        double distance = offset.length();

        double anchorOffset = 0.45;
        double idealScale = 0.65;

        double endClip = distance - anchorOffset;
        double totalDistance = endClip - anchorOffset;

        if (totalDistance > 0.4) {
            int linkCount = (int) Math.max(1, Math.ceil(totalDistance / idealScale));
            double actualStep = totalDistance / linkCount;

            for (int i = 0; i < linkCount; i++) {
                double d = anchorOffset + (i * actualStep) + (actualStep / 2.0);

                matrices.pushPose();
                matrices.translate(0.0, 0.0, d);
                matrices.scale((float) idealScale, (float) idealScale, (float) idealScale);
                matrices.translate(-0.5, -0.5, -0.5);
                this.submitMinecartContents(state, chainState, matrices, queue, state.lightCoords);
                matrices.popPose();
            }
        }

        matrices.popPose();
    }
}