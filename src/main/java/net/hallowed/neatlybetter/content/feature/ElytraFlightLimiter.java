package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ElytraFlightLimiter {
    private ElytraFlightLimiter() {}

    public static void init() {
        EntityElytraEvents.ALLOW.register((LivingEntity entity) -> {
            if (NTServerConfig.CONFIG.elytraFlightLimiter.isFalse()) return true;
            if (!(entity instanceof Player p)) return true;
            if (p.isFallFlying()) return true;

            double minClearance = NTServerConfig.CONFIG.elytraFlightLimiterMinClearance.get();
            double bypassFallDistance = NTServerConfig.CONFIG.elytraFlightLimiterBypassFallDistance.get();

            double feetY = p.getBoundingBox().minY;
            Vec3 start = new Vec3(p.getX(), feetY, p.getZ());
            Vec3 end   = new Vec3(p.getX(), feetY - (minClearance + 0.05), p.getZ());

            BlockHitResult hit = p.level().clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    p
            ));

            if (hit.getType() == HitResult.Type.BLOCK) {
                double clearance = feetY - hit.getLocation().y;

                if (p.fallDistance > bypassFallDistance) {
                    return true;
                }

                return !(clearance < minClearance);
            }

            return true;
        });
    }
}