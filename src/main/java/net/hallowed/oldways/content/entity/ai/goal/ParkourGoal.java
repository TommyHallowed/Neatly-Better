package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.LongJumpUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Optional;

public class ParkourGoal extends Goal {

    private static final int MAX_GAP_BLOCKS = 2;
    private static final double TICK_SECONDS = 1.0 / 20.0;
    private static final double EPS = 1e-3;

    private final MobEntity mob;

    private BlockPos takeoffBlock = null;
    private Vec3d    takeoffPoint = null;
    private BlockPos landingBlock = null;
    private Vec3d    plannedVel   = null;

    private int stepX = 0, stepZ = 0;

    private int gapBlocks = 0;

    private boolean jumped = false;

    private double lastClearance = Double.POSITIVE_INFINITY;
    private int    stallTicks    = 0;

    public ParkourGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (mob.getWorld().getDifficulty() != Difficulty.HARD) return false;
        if (!mob.isOnGround() || !mob.isAlive()) return false;
        if (mob.isTouchingWater()) return false;

        {
            float yaw = mob.getYaw() * (float)(Math.PI / 180.0);
            double fx = -MathHelper.sin(yaw);
            double fz =  MathHelper.cos(yaw);
            if (Math.abs(fx) >= Math.abs(fz)) { stepX = fx >= 0 ? 1 : -1; stepZ = 0; }
            else                              { stepX = 0;       stepZ = fz >= 0 ? 1 : -1; }
        }

        World w = mob.getWorld();
        BlockPos feet = mob.getBlockPos();
        BlockPos belowFeet = feet.down();

        if (!isSolidTop(w, belowFeet)) return false;
        if (!hasHeadroom(w, feet))     return false;

        int gap = 0;
        BlockPos edgeBelow = null;

        for (int i = 1; i <= MAX_GAP_BLOCKS + 2; i++) {
            BlockPos checkBelow = belowFeet.add(stepX * i, 0, stepZ * i);
            boolean solidBelow = isSolidTop(w, checkBelow);

            if (gap == 0) {
                if (!solidBelow) {
                    gap = 1;
                    edgeBelow = belowFeet.add(stepX * (i - 1), 0, stepZ * (i - 1));
                }
            } else {
                if (!solidBelow) {
                    if (++gap > MAX_GAP_BLOCKS) return false; // too wide
                } else {
                    BlockPos landFeet = checkBelow.up();
                    if (!hasHeadroom(w, landFeet)) return false;

                    BlockPos takeoffFeet = edgeBelow.up();
                    if (!hasHeadroom(w, takeoffFeet)) return false;
                    if (!hasClearGapCeiling(w, takeoffFeet, stepX, stepZ, gap)) return false;

                    this.takeoffBlock = takeoffFeet;
                    this.takeoffPoint = computeEdgeSafeTakeoffPoint(mob, takeoffFeet, stepX, stepZ);
                    this.landingBlock = landFeet;
                    this.gapBlocks    = gap;

                    Optional<Vec3d> v = computeJumpVelocity(mob, Vec3d.ofCenter(landFeet));
                    if (v.isEmpty()) return false;

                    Vec3d solved = v.get();

                    if (this.gapBlocks >= 2) {
                        boolean overhangAtLanding = !noCollision(w, landFeet.up(3));
                        double mult = overhangAtLanding ? 1.55 : 1.30;
                        solved = new Vec3d(solved.x * mult, solved.y, solved.z * mult);
                    }
                    // ------------------------------------------------------------

                    this.plannedVel    = solved;
                    this.jumped        = false;
                    this.lastClearance = Double.POSITIVE_INFINITY;
                    this.stallTicks    = 0;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return mob.isAlive()
                && !jumped
                && takeoffBlock  != null
                && landingBlock  != null
                && takeoffPoint  != null
                && plannedVel    != null;
    }

    @Override public void start() {}
    @Override public void stop()  { clearPlan(); mob.getNavigation().stop(); }

    @Override
    public void tick() {
        if (!shouldContinue()) return;

        Vec3d look = preferredLook();
        mob.getLookControl().lookAt(look.x, look.y, look.z);

        if (!mob.getBlockPos().equals(this.takeoffBlock)) {
            BlockPos behind = this.takeoffBlock.add(-stepX, 0, -stepZ);
            double closeR   = 1.0 + mob.getWidth();
            double d2       = mob.squaredDistanceTo(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z);

            if (mob.getBlockPos().equals(behind) || d2 <= closeR * closeR) {
                mob.getNavigation().stop();
                mob.getMoveControl().moveTo(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
                return;
            }

            boolean ok = mob.getNavigation().startMovingTo(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
            if (!ok) mob.getMoveControl().moveTo(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
            return;
        }

        double clearance = frontClearanceToLip(mob, takeoffBlock, stepX, stepZ);

        if (clearance > 0.0) {
            mob.getNavigation().stop();
            mob.getMoveControl().moveTo(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);

            if (clearance > lastClearance - 1e-4) {
                if (++stallTicks >= 6) { // hard-coded stall threshold
                    double n = Math.min(0.06, Math.max(0.015, clearance * 0.25));
                    mob.addVelocity(stepX * n, 0.0, stepZ * n);
                    stallTicks = 0;
                }
            } else {
                stallTicks = 0;
            }
            lastClearance = clearance;

            double forwardSpeed = (stepX != 0 ? mob.getVelocity().x * stepX
                    : mob.getVelocity().z * stepZ);
            double predictedAdvance = Math.max(0.0, forwardSpeed * TICK_SECONDS);

            if (clearance > predictedAdvance + EPS) {
                return;
            }
        }

        if (!jumped && mob.isOnGround()) {
            mob.getNavigation().stop();
            mob.getJumpControl().setActive();
            mob.setVelocity(this.plannedVel);
            jumped = true;
            clearPlan();
        }
    }

    /* ---------------- helpers ---------------- */

    private static Vec3d computeEdgeSafeTakeoffPoint(MobEntity mob, BlockPos takeoffFeet, int sx, int sz) {
        double half = mob.getWidth() * 0.5;
        double cx = takeoffFeet.getX() + 0.5;
        double cz = takeoffFeet.getZ() + 0.5;

        if (sx != 0) {
            double lipX = (sx > 0) ? (takeoffFeet.getX() + 1.0) : takeoffFeet.getX();
            double x = lipX - sx * (half + EPS); // stay inside tile by half width
            return new Vec3d(x, mob.getY(), cz);
        } else {
            double lipZ = (sz > 0) ? (takeoffFeet.getZ() + 1.0) : takeoffFeet.getZ();
            double z = lipZ - sz * (half + EPS);
            return new Vec3d(cx, mob.getY(), z);
        }
    }

    private static double frontClearanceToLip(MobEntity m, BlockPos tf, int sx, int sz) {
        double half = m.getWidth() * 0.5;
        if (sx != 0) {
            double front = m.getX() + sx * half;
            double lipX  = (sx > 0) ? (tf.getX() + 1.0) : tf.getX();
            return (lipX - front) * sx;
        } else {
            double front = m.getZ() + sz * half;
            double lipZ  = (sz > 0) ? (tf.getZ() + 1.0) : tf.getZ();
            return (lipZ - front) * sz;
        }
    }

    private Vec3d preferredLook() {
        LivingEntity t = mob.getTarget();
        if (t != null && t.isAlive()) return t.getEyePos();
        if (landingBlock != null)    return Vec3d.ofCenter(landingBlock);
        if (takeoffPoint  != null)   return takeoffPoint;
        return mob.getPos();
    }

    private void clearPlan() {
        takeoffBlock  = null;
        takeoffPoint  = null;
        landingBlock  = null;
        plannedVel    = null;
        lastClearance = Double.POSITIVE_INFINITY;
        stallTicks    = 0;
        gapBlocks     = 0;
    }


    private static boolean isSolidTop(World w, BlockPos posBelow) {
        var state = w.getBlockState(posBelow);
        return !state.getCollisionShape(w, posBelow).isEmpty();
    }

    private static boolean hasHeadroom(World w, BlockPos feet) {
        return noCollision(w, feet)
                && noCollision(w, feet.up())
                && noCollision(w, feet.up(2));
    }

    private static boolean hasClearGapCeiling(World w, BlockPos takeoffFeet, int stepX, int stepZ, int gap) {
        for (int t = 1; t <= gap; t++) {
            BlockPos feet = takeoffFeet.add(stepX * t, 0, stepZ * t);
            if (!hasHeadroom(w, feet)) return false;
        }
        return true;
    }

    private static boolean noCollision(World w, BlockPos pos) {
        var state = w.getBlockState(pos);
        return state.getCollisionShape(w, pos).isEmpty();
    }

    private static Optional<Vec3d> computeJumpVelocity(MobEntity mob, Vec3d landingCenter) {
        for (int angle = 50; angle <= 85; angle += 5) {
            Optional<Vec3d> v = LongJumpUtil.getJumpingVelocity(mob, landingCenter, 1.6F, angle, true);
            if (v.isPresent()) return v;
        }
        return Optional.empty();
    }
}
