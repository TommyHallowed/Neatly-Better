package net.hallowed.oldways.client.util;

import net.minecraft.util.math.Vec3d;

public interface ChainableRenderState {
    Vec3d oldways$getLinkedPos();
    void oldways$setLinkedPos(Vec3d pos);

    double oldways$getStartOffset();
    void oldways$setStartOffset(double offset);

    double oldways$getEndOffset();
    void oldways$setEndOffset(double offset);

    Vec3d oldways$getVisualOffset();
    void oldways$setVisualOffset(Vec3d offset);
}