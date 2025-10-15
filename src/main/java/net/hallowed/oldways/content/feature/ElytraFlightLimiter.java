package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class ElytraFlightLimiter {
    private ElytraFlightLimiter() {}

    public static double MIN_CLEARANCE_BLOCKS = 4.0;

    public static float BYPASS_FALL_DISTANCE = 5.0F;

    public static void init() {
        EntityElytraEvents.ALLOW.register((LivingEntity entity) -> {
            if (!(entity instanceof PlayerEntity p)) return true;
            if (p.isGliding()) return true;

            double feetY = p.getBoundingBox().minY;
            Vec3d start = new Vec3d(p.getX(), feetY, p.getZ());
            Vec3d end   = new Vec3d(p.getX(), feetY - (MIN_CLEARANCE_BLOCKS + 0.05), p.getZ());

            BlockHitResult hit = p.getEntityWorld().raycast(new RaycastContext(
                    start, end,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    p
            ));

            if (hit.getType() == HitResult.Type.BLOCK) {
                double clearance = feetY - hit.getPos().y;

                if (p.fallDistance > BYPASS_FALL_DISTANCE) {
                    return true;
                }

                return !(clearance < MIN_CLEARANCE_BLOCKS);
            }

            return true;
        });
    }
}
