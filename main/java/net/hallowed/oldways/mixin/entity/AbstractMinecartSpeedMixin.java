package net.hallowed.oldways.mixin.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartSpeedMixin {

    // Tunables
    @Unique private static final double MAX_BPS = 48.0;       // blocks per second
    @Unique private static final double ACCEL_PER_TICK = 0.2; // +0.6 b/s each tick (~12 b/s/s)
    @Unique private static final double START_THRESHOLD_BPS = 0.10; // require tiny motion
    @Unique private static final double BLEND = 0.25;         // smoothing factor toward target

    @Inject(method = "tick", at = @At("TAIL"))
    private void hallowed$gradualAccel(CallbackInfo ci) {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;
        World world = self.getWorld();
        if (world.isClient()) return;

        // Only modify carts that have a PLAYER rider
        if (!hasPlayerRider(self)) return;

        // Only affect carts on rails
        if (!isOnRails(world, self.getBlockPos())) return;

        Vec3d v = self.getVelocity();
        double horiz = Math.hypot(v.x, v.z);
        double curBps = horiz * 20.0;

        // Don’t auto-start carts; require minimal motion
        if (curBps <= START_THRESHOLD_BPS) return;

        // Gradually raise speed up to cap
        double nextBps = Math.min(MAX_BPS, curBps + ACCEL_PER_TICK);
        if (nextBps <= curBps) return;

        if (horiz > 1.0e-9) {
            double targetPerTick = nextBps / 20.0;
            double scale = targetPerTick / horiz;
            Vec3d desired = new Vec3d(v.x * scale, v.y, v.z * scale);

            self.setVelocity(
                    MathHelper.lerp(BLEND, v.x, desired.x),
                    v.y,
                    MathHelper.lerp(BLEND, v.z, desired.z)
            );
        }
    }

    // Allow higher clamp only when on rails AND a player is riding
    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    private void hallowed$boostedMaxSpeed(CallbackInfoReturnable<Double> cir) {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;
        if (hasPlayerRider(self) && isOnRails(self.getWorld(), self.getBlockPos())) {
            cir.setReturnValue(MAX_BPS / 20.0); // blocks per tick
        }
    }

    // --- helpers ---
    @Unique
    private static boolean hasPlayerRider(AbstractMinecartEntity cart) {
        Entity e = cart.getFirstPassenger();
        return e instanceof PlayerEntity;
    }

    @Unique
    private static boolean isOnRails(World world, BlockPos pos) {
        BlockState s = world.getBlockState(pos);
        if (s.isIn(BlockTags.RAILS)) return true;
        s = world.getBlockState(pos.down());
        return s.isIn(BlockTags.RAILS);
    }
}
