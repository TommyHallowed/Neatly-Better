package net.hallowed.oldways.mixin.entity.misc;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
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
public abstract class AbstractMinecartEntityMixin {

    @Unique private static final double ACCEL_PER_TICK = 0.1;    // +0.2 b/s per tick (~4 b/s/s)
    @Unique private static final double START_THRESHOLD_BPS = 0.1;
    @Unique private static final double BLEND = 0.5;             // smoothing toward target


    @Inject(method = "tick", at = @At("TAIL"))
    private void oldways$gradualAccel(CallbackInfo ci) {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;
        World world = self.getEntityWorld();
        if (!(world instanceof ServerWorld sw)) return;

        if (!hasPlayerRider(self) || !isOnRails(sw, self.getBlockPos())) return;

        final int capBps = getCapBps(sw);
        if (capBps <= 0) return;

        Vec3d v = self.getVelocity();
        double horiz = Math.hypot(v.x, v.z);
        double curBps = horiz * 20.0;

        if (curBps <= START_THRESHOLD_BPS) return;

        double nextBps = Math.min(capBps, curBps + ACCEL_PER_TICK);
        if (nextBps <= curBps || horiz <= 1.0e-9) return;

        double targetPerTick = nextBps / 20.0;
        double scale = targetPerTick / horiz;
        Vec3d desired = new Vec3d(v.x * scale, v.y, v.z * scale);

        self.setVelocity(
                MathHelper.lerp(BLEND, v.x, desired.x),
                v.y,
                MathHelper.lerp(BLEND, v.z, desired.z)
        );
    }

    @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
    private void oldways$boostedMaxSpeed(CallbackInfoReturnable<Double> cir) {
        AbstractMinecartEntity self = (AbstractMinecartEntity)(Object)this;
        World world = self.getEntityWorld();
        if (!(world instanceof ServerWorld sw)) return;

        if (hasPlayerRider(self) && isOnRails(sw, self.getBlockPos())) {
            int capBps = getCapBps(sw);
            if (capBps > 0) {
                cir.setReturnValue(capBps / 20.0);
            }
        }
    }

    /* ---------------- helpers ---------------- */

    @Unique
    private static int getCapBps(ServerWorld sw) {
        return Math.max(0, sw.getGameRules().getInt(ModGameRules.MAX_MINECART_SPEED));
    }

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