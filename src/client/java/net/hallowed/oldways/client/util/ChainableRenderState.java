package net.hallowed.oldways.client.util;

import net.minecraft.world.phys.Vec3;

public interface ChainableRenderState {
    Vec3 oldways$getLinkedPos();
    void oldways$setLinkedPos(Vec3 pos);

    double oldways$getStartOffset();
    void oldways$setStartOffset(double offset);

    double oldways$getEndOffset();
    void oldways$setEndOffset(double offset);

    Vec3 oldways$getVisualOffset();
    void oldways$setVisualOffset(Vec3 offset);
}