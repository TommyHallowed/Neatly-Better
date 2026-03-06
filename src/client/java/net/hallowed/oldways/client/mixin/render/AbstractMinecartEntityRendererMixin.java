package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.api.LinkableMinecart;
import net.hallowed.oldways.client.util.ChainableRenderState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.AbstractMinecartEntityRenderer;
import net.minecraft.client.render.entity.state.MinecartEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntityRenderer.class)
public abstract class AbstractMinecartEntityRendererMixin<T extends AbstractMinecartEntity, S extends MinecartEntityRenderState> {

    @Shadow protected abstract void renderBlock(S state, BlockState blockState, MatrixStack matrices, OrderedRenderCommandQueue queue, int light);

    @Unique
    private Vec3d oldways$getVisualPos(AbstractMinecartEntity cart, float tickDelta) {
        Vec3d rawPos = cart.getLerpedPos(tickDelta);
        if (cart.getController() instanceof DefaultMinecartController def) {
            Vec3d present = def.snapPositionToRail(rawPos.x, rawPos.y, rawPos.z);
            if (present != null) {
                Vec3d future = def.simulateMovement(rawPos.x, rawPos.y, rawPos.z, 0.3);
                Vec3d past = def.simulateMovement(rawPos.x, rawPos.y, rawPos.z, -0.3);
                double y = ((future != null ? future.y : present.y) + (past != null ? past.y : present.y)) / 2.0;
                return new Vec3d(present.x, y, present.z);
            }
        }
        return rawPos;
    }

    // Still includes your frustum culling fix just to be safe!
    @Inject(method = "getBoundingBox*", at = @At("RETURN"), cancellable = true)
    private void oldways$expandRenderBox(T entity, CallbackInfoReturnable<Box> cir) {
        int linkedId = ((LinkableMinecart) entity).oldways$getFollowingId();
        if (linkedId != -1) {
            Entity e = entity.getEntityWorld().getEntityById(linkedId);
            if (e instanceof AbstractMinecartEntity linked) {
                cir.setReturnValue(cir.getReturnValue().union(linked.getBoundingBox()));
            }
        }
    }

    @Inject(method = "updateRenderState*", at = @At("TAIL"))
    private void oldways$updateChainState(T entity, S state, float tickDelta, CallbackInfo ci) {
        int linkedId = ((LinkableMinecart) entity).oldways$getFollowingId();
        AbstractMinecartEntity linked = null;

        if (linkedId != -1) {
            Entity e = entity.getEntityWorld().getEntityById(linkedId);
            if (e instanceof AbstractMinecartEntity targetCart) {
                linked = targetCart;
            }
        }

        if (linked != null) {
            Vec3d visualMyPos = oldways$getVisualPos(entity, tickDelta);
            Vec3d visualTargetPos = oldways$getVisualPos(linked, tickDelta);

            Vec3d rawPos = new Vec3d(state.x, state.y, state.z);
            ((ChainableRenderState) state).oldways$setVisualOffset(visualMyPos.subtract(rawPos));
            ((ChainableRenderState) state).oldways$setLinkedPos(visualTargetPos.subtract(visualMyPos));


        } else {
            ((ChainableRenderState) state).oldways$setLinkedPos(null);
        }
    }

    @Inject(method = "render*", at = @At("TAIL"))
    private void oldways$renderChainVisuals(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        Vec3d offset = ((ChainableRenderState) state).oldways$getLinkedPos();
        if (offset == null) return;

        Vec3d visualOffset = ((ChainableRenderState) state).oldways$getVisualOffset();

        matrices.push();
        matrices.translate(visualOffset.x, visualOffset.y + 0.375, visualOffset.z);

        double dx = offset.x;
        double dy = offset.y;
        double dz = offset.z;

        float yaw = (float) (MathHelper.atan2(dx, dz) * 180.0F / Math.PI);
        float pitch = (float) -(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0F / Math.PI);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

        BlockState chainState = Blocks.IRON_CHAIN.getDefaultState().with(Properties.AXIS, Direction.Axis.Z);

        double distance = offset.length();

        double anchorOffset = 0.45;
        double idealScale = 0.65;

        double endClip = distance - anchorOffset;
        double totalDistance = endClip - anchorOffset;

        // --- FIX: NO MORE FAILSAFE ---
        // If the gap is completely closed (carts are touching), it simply won't draw anything!
        if (totalDistance > 0.4) {
            int linkCount = (int) Math.max(1, Math.ceil(totalDistance / idealScale));
            double actualStep = totalDistance / linkCount;

            for (int i = 0; i < linkCount; i++) {
                double d = anchorOffset + (i * actualStep) + (actualStep / 2.0);

                matrices.push();
                matrices.translate(0.0, 0.0, d);
                matrices.scale((float) idealScale, (float) idealScale, (float) idealScale);
                matrices.translate(-0.5, -0.5, -0.5);
                this.renderBlock(state, chainState, matrices, queue, state.light);
                matrices.pop();
            }
        }

        matrices.pop();
    }
}