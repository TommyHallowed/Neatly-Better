package net.hallowed.oldways.client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public final class GameRendererPickHelper {
    private GameRendererPickHelper() {}

    public static HitResult pickIgnoringOutlineOnly(Entity camera, double blockRange, double entityRange, float tickProgress) {
        Level world = camera.level();

        double d = Math.max(blockRange, entityRange);
        double maxSq = d * d;

        Vec3 start = camera.getEyePosition(tickProgress);
        Vec3 dir   = camera.getViewVector(tickProgress);
        Vec3 end   = start.add(dir.x * d, dir.y * d, dir.z * d);

        BlockHitResult blockHit = world.clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                camera
        ));

        HitResult best = blockHit;
        double bestSq = blockHit.getLocation().distanceToSqr(start);

        AABB box = camera.getBoundingBox().expandTowards(dir.scale(d)).inflate(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(camera, start, end, box, EntitySelector.CAN_BE_PICKED, maxSq);

        if (entityHit != null) {
            double entSq = entityHit.getLocation().distanceToSqr(start);
            if (entSq < bestSq || best.getType() == Type.MISS) {
                best = entityHit;
                //noinspection UnusedAssignment
                bestSq = entSq;
            }
        }

        return ensureInRange(best, start, (best instanceof EntityHitResult) ? entityRange : blockRange);
    }

    private static HitResult ensureInRange(HitResult hit, Vec3 cameraPos, double range) {
        Vec3 hp = hit.getLocation();
        if (!hp.closerThan(cameraPos, range)) {
            Direction dir = Direction.getApproximateNearest(hp.x - cameraPos.x, hp.y - cameraPos.y, hp.z - cameraPos.z);
            return BlockHitResult.miss(hp, dir, BlockPos.containing(hp));
        }
        return hit;
    }
}
