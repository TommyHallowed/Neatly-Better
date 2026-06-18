package net.hallowed.neatlybetter.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.hallowed.neatlybetter.api.LinkableMinecart;
import net.hallowed.neatlybetter.client.util.ChainableRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartRenderer.class)
public abstract class AbstractMinecartRendererMixin<T extends AbstractMinecart, S extends MinecartRenderState> {

    @Shadow
    protected abstract void submitMinecartContents(S state, BlockModelRenderState blockModel, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords);

    @Final
    @Shadow
    private BlockModelResolver blockModelResolver;

    @Unique
    private BlockModelRenderState neatlybetter$chainBlockModel;

    @Unique
    private Vec3 neatlybetter$getVisualPos(AbstractMinecart cart, float tickDelta) {
        Vec3 rawPos = cart.getPosition(tickDelta);
        if (cart.getBehavior() instanceof OldMinecartBehavior def) {
            Vec3 present = def.getPos(rawPos.x, rawPos.y, rawPos.z);
            if (present != null) {
                Vec3 future = def.getPosOffs(rawPos.x, rawPos.y, rawPos.z, 0.3);
                Vec3 past   = def.getPosOffs(rawPos.x, rawPos.y, rawPos.z, -0.3);
                double y = ((future != null ? future.y : present.y) + (past != null ? past.y : present.y)) / 2.0;
                return new Vec3(present.x, y, present.z);
            }
        }
        return rawPos;
    }

    @Inject(method = "getBoundingBoxForCulling*", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$expandRenderBox(T entity, CallbackInfoReturnable<AABB> cir) {
        int linkedId = ((LinkableMinecart) entity).neatlybetter$getFollowingId();
        if (linkedId != -1) {
            Entity e = entity.level().getEntity(linkedId);
            if (e instanceof AbstractMinecart linked) {
                cir.setReturnValue(cir.getReturnValue().minmax(linked.getBoundingBox()));
            }
        }
    }

    @Inject(method = "extractRenderState*", at = @At("TAIL"))
    private void neatlybetter$updateChainState(T entity, S state, float partialTicks, CallbackInfo ci) {
        int linkedId = ((LinkableMinecart) entity).neatlybetter$getFollowingId();
        AbstractMinecart linked = null;

        if (linkedId != -1) {
            Entity e = entity.level().getEntity(linkedId);
            if (e instanceof AbstractMinecart targetCart) {
                linked = targetCart;
            }
        }

        if (linked != null) {
            Vec3 visualMyPos     = neatlybetter$getVisualPos(entity, partialTicks);
            Vec3 visualTargetPos = neatlybetter$getVisualPos(linked, partialTicks);
            Vec3 rawPos = new Vec3(state.x, state.y, state.z);
            ((ChainableRenderState) state).neatlybetter$setVisualOffset(visualMyPos.subtract(rawPos));
            ((ChainableRenderState) state).neatlybetter$setLinkedPos(visualTargetPos.subtract(visualMyPos));
        } else {
            ((ChainableRenderState) state).neatlybetter$setLinkedPos(null);
        }
    }

    @Inject(method = "submit*", at = @At("TAIL"))
    private void neatlybetter$renderChainVisuals(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        Vec3 offset = ((ChainableRenderState) state).neatlybetter$getLinkedPos();
        if (offset == null) return;

        Vec3 visualOffset = ((ChainableRenderState) state).neatlybetter$getVisualOffset();

        if (neatlybetter$chainBlockModel == null) {
            neatlybetter$chainBlockModel = new BlockModelRenderState();
        }
        BlockState chainBlockState = Blocks.IRON_CHAIN.defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
        blockModelResolver.update(neatlybetter$chainBlockModel, chainBlockState,
                AbstractMinecartRenderer.BLOCK_DISPLAY_CONTEXT);

        poseStack.pushPose();
        poseStack.translate(visualOffset.x, visualOffset.y + 0.375, visualOffset.z);

        double dx = offset.x;
        double dy = offset.y;
        double dz = offset.z;

        float yaw   = (float) (Mth.atan2(dx, dz) * 180.0F / Math.PI);
        float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0F / Math.PI);

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        double distance     = offset.length();
        double anchorOffset = 0.45;
        double idealScale   = 0.65;
        double endClip      = distance - anchorOffset;
        double totalDistance = endClip - anchorOffset;

        if (totalDistance > 0.4) {
            int    linkCount   = (int) Math.max(1, Math.ceil(totalDistance / idealScale));
            double actualStep  = totalDistance / linkCount;

            for (int i = 0; i < linkCount; i++) {
                double d = anchorOffset + (i * actualStep) + (actualStep / 2.0);
                poseStack.pushPose();
                poseStack.translate(0.0, 0.0, d);
                poseStack.scale((float) idealScale, (float) idealScale, (float) idealScale);
                poseStack.translate(-0.5, -0.5, -0.5);
                this.submitMinecartContents(state, neatlybetter$chainBlockModel, poseStack, submitNodeCollector, state.lightCoords);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }
}