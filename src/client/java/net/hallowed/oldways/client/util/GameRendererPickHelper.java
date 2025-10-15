package net.hallowed.oldways.client.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public final class GameRendererPickHelper {
    private GameRendererPickHelper() {}

    public static HitResult pickIgnoringOutlineOnly(Entity camera, double blockRange, double entityRange, float tickProgress) {
        World world = camera.getEntityWorld();
        if (world == null) return null;

        double d = Math.max(blockRange, entityRange);
        double maxSq = d * d;

        Vec3d start = camera.getCameraPosVec(tickProgress);
        Vec3d dir   = camera.getRotationVec(tickProgress);
        Vec3d end   = start.add(dir.x * d, dir.y * d, dir.z * d);

        BlockHitResult blockHit = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                camera
        ));

        HitResult best = blockHit;
        double bestSq = blockHit.getPos().squaredDistanceTo(start);

        Box box = camera.getBoundingBox().stretch(dir.multiply(d)).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHit = ProjectileUtil.raycast(camera, start, end, box, EntityPredicates.CAN_HIT, maxSq);

        if (entityHit != null) {
            double entSq = entityHit.getPos().squaredDistanceTo(start);
            if (entSq < bestSq || best.getType() == Type.MISS) {
                best = entityHit;
                //noinspection UnusedAssignment
                bestSq = entSq;
            }
        }

        return ensureInRange(best, start, (best instanceof EntityHitResult) ? entityRange : blockRange);
    }

    private static HitResult ensureInRange(HitResult hit, Vec3d cameraPos, double range) {
        Vec3d hp = hit.getPos();
        if (!hp.isInRange(cameraPos, range)) {
            Direction dir = Direction.getFacing(hp.x - cameraPos.x, hp.y - cameraPos.y, hp.z - cameraPos.z);
            return BlockHitResult.createMissed(hp, dir, BlockPos.ofFloored(hp));
        }
        return hit;
    }
}
