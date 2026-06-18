package net.hallowed.neatlybetter.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.hallowed.neatlybetter.client.render.SignRenderKeys;
import net.hallowed.neatlybetter.content.feature.InvisibleSign;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin<S extends SignRenderState> {

    @Unique
    private boolean neatlybetter$currentlyInvisible = false;
    @Unique
    private boolean neatlybetter$currentlyWallSign = false;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/level/block/entity/SignBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL")
    )
    private void neatlybetter$extractInvisible(
            SignBlockEntity blockEntity, SignRenderState state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress,
            CallbackInfo ci) {
        boolean invisible = Boolean.TRUE.equals(blockEntity.getAttached(InvisibleSign.INVISIBLE));
        state.setData(SignRenderKeys.INVISIBLE, invisible ? Boolean.TRUE : null);
    }

    @Inject(method = "submit*", at = @At("HEAD"))
    private void neatlybetter$captureFlags(SignRenderState state, PoseStack poseStack,
                                           SubmitNodeCollector submitNodeCollector,
                                           CameraRenderState camera,
                                           CallbackInfo ci) {
        Boolean v = state.getData(SignRenderKeys.INVISIBLE);
        this.neatlybetter$currentlyInvisible = v != null && v;
        this.neatlybetter$currentlyWallSign = state.blockState.getBlock() instanceof WallSignBlock;
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/AbstractSignRenderer;submitSignText(Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/level/block/entity/SignText;)V"
            )
    )
    private void neatlybetter$nudgeTextIntoWall(
            SignRenderState state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            CallbackInfo ci) {
        if (this.neatlybetter$currentlyInvisible && this.neatlybetter$currentlyWallSign) {
            poseStack.translate(0.0, 0.0, -10.2);
        }
    }
}