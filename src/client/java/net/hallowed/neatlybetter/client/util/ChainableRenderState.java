package net.hallowed.neatlybetter.client.util;

import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public interface ChainableRenderState {
    Vec3 neatlybetter$getLinkedPos();
    void neatlybetter$setLinkedPos(Vec3 pos);

    double neatlybetter$getStartOffset();
    void neatlybetter$setStartOffset(double offset);

    double neatlybetter$getEndOffset();
    void neatlybetter$setEndOffset(double offset);

    Vec3 neatlybetter$getVisualOffset();
    void neatlybetter$setVisualOffset(Vec3 offset);
}