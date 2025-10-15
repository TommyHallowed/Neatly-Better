package net.hallowed.oldways.content.entity.vehicle;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class LavaBoatEntity extends BoatEntity {

    public LavaBoatEntity(EntityType<? extends BoatEntity> type,
                          World world,
                          Supplier<Item> dropItem) {
        super(type, world, dropItem);
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInLava()) {
            Vec3d v = this.getVelocity();
            double slow = 1.2;
            double vx = v.x * slow;
            double vz = v.z * slow;

            BlockPos pos = this.getBlockPos();
            FluidState lava = this.getEntityWorld().getFluidState(pos);

            double surfaceY = pos.getY() + lava.getHeight(this.getEntityWorld(), pos);

            final double targetSubmersion = 0.16;

            double bottomY = this.getBoundingBox().minY;
            double liftNeeded = surfaceY - targetSubmersion - bottomY;

            double vy = Math.max(v.y, 0.0) + Math.max(0.0, Math.min(liftNeeded, 0.10));

            this.setVelocity(vx, vy, vz);
        }
    }
}
