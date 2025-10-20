package net.hallowed.oldways.client.mixin.entity;

import net.hallowed.oldways.client.accessor.EntitySoulFireAccessor;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntitySoulFireMixin implements EntitySoulFireAccessor {
    @Unique
    private boolean oldways$soulFire = false;

    @Unique
    public boolean oldways$isSoulFire() {
        return this.oldways$soulFire;
    }

    @Unique
    public void oldways$setSoulFire(boolean val) {
        this.oldways$soulFire = val;
    }
}

