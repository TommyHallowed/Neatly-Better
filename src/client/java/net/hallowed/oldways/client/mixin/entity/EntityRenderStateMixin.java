package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.accessor.EntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateAccessor {
    @Unique
    private boolean oldways$soulFire = false;

    // reflection-accessible methods used by other mixins (names must match)
    @Unique
    public boolean oldways$isSoulFire() {
        return this.oldways$soulFire;
    }

    @Unique
    public void oldways$setSoulFire(boolean val) {
        this.oldways$soulFire = val;
    }
}
