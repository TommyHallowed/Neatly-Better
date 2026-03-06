package net.hallowed.oldways.client.mixin.render;

import net.hallowed.oldways.client.util.ChainableRenderState;
import net.minecraft.client.render.entity.state.MinecartEntityRenderState;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecartEntityRenderState.class)
public class MinecartEntityRenderStateMixin implements ChainableRenderState {
    @Unique private Vec3d oldways$linkedPos;
    @Unique private double oldways$startOffset;
    @Unique private double oldways$endOffset;
    @Unique private Vec3d oldways$visualOffset;

    @Override public Vec3d oldways$getLinkedPos() { return this.oldways$linkedPos; }
    @Override public void oldways$setLinkedPos(Vec3d pos) { this.oldways$linkedPos = pos; }

    @Override public double oldways$getStartOffset() { return this.oldways$startOffset; }
    @Override public void oldways$setStartOffset(double offset) { this.oldways$startOffset = offset; }

    @Override public double oldways$getEndOffset() { return this.oldways$endOffset; }
    @Override public void oldways$setEndOffset(double offset) { this.oldways$endOffset = offset; }

    @Override public Vec3d oldways$getVisualOffset() { return this.oldways$visualOffset; }
    @Override public void oldways$setVisualOffset(Vec3d offset) { this.oldways$visualOffset = offset; }
}