package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import java.util.Optional;

public class ParkourGoal extends Goal {

    private static final int MAX_GAP_BLOCKS = 2;
    private static final double TICK_SECONDS = 1.0 / 20.0;
    private static final double EPS = 1e-3;

    private final Mob mob;

    private BlockPos takeoffBlock = null;
    private Vec3    takeoffPoint = null;
    private BlockPos landingBlock = null;
    private Vec3    plannedVel   = null;

    private int stepX = 0, stepZ = 0;

    private int gapBlocks = 0;

    private boolean jumped = false;

    private double lastClearance = Double.POSITIVE_INFINITY;
    private int    stallTicks    = 0;

    public ParkourGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (mob.level().getDifficulty() != Difficulty.HARD) return false;
        if (!mob.onGround() || !mob.isAlive()) return false;
        if (mob.isInWater()) return false;

        {
            float yaw = mob.getYRot() * (float)(Math.PI / 180.0);
            double fx = -Mth.sin(yaw);
            double fz =  Mth.cos(yaw);
            if (Math.abs(fx) >= Math.abs(fz)) { stepX = fx >= 0 ? 1 : -1; stepZ = 0; }
            else                              { stepX = 0;       stepZ = fz >= 0 ? 1 : -1; }
        }

        Level w = mob.level();
        BlockPos feet = mob.blockPosition();
        BlockPos belowFeet = feet.below();

        if (!isSolidTop(w, belowFeet)) return false;
        if (!hasHeadroom(w, feet))     return false;

        int gap = 0;
        BlockPos edgeBelow = null;

        for (int i = 1; i <= MAX_GAP_BLOCKS + 2; i++) {
            BlockPos checkBelow = belowFeet.offset(stepX * i, 0, stepZ * i);
            boolean solidBelow = isSolidTop(w, checkBelow);

            if (gap == 0) {
                if (!solidBelow) {
                    gap = 1;
                    edgeBelow = belowFeet.offset(stepX * (i - 1), 0, stepZ * (i - 1));
                }
            } else {
                if (!solidBelow) {
                    if (++gap > MAX_GAP_BLOCKS) return false; // too wide
                } else {
                    BlockPos landFeet = checkBelow.above();
                    if (!hasHeadroom(w, landFeet)) return false;

                    BlockPos takeoffFeet = edgeBelow.above();
                    if (!hasHeadroom(w, takeoffFeet)) return false;
                    if (!hasClearGapCeiling(w, takeoffFeet, stepX, stepZ, gap)) return false;

                    this.takeoffBlock = takeoffFeet;
                    this.takeoffPoint = computeEdgeSafeTakeoffPoint(mob, takeoffFeet, stepX, stepZ);
                    this.landingBlock = landFeet;
                    this.gapBlocks    = gap;

                    Optional<Vec3> v = computeJumpVelocity(mob, Vec3.atCenterOf(landFeet));
                    if (v.isEmpty()) return false;

                    Vec3 solved = v.get();

                    if (this.gapBlocks >= 2) {
                        boolean overhangAtLanding = !noCollision(w, landFeet.above(3));
                        double mult = overhangAtLanding ? 1.55 : 1.30;
                        solved = new Vec3(solved.x * mult, solved.y, solved.z * mult);
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
    public boolean canContinueToUse() {
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
        if (!canContinueToUse()) return;

        Vec3 look = preferredLook();
        mob.getLookControl().setLookAt(look.x, look.y, look.z);

        if (!mob.blockPosition().equals(this.takeoffBlock)) {
            BlockPos behind = this.takeoffBlock.offset(-stepX, 0, -stepZ);
            double closeR   = 1.0 + mob.getBbWidth();
            double d2       = mob.distanceToSqr(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z);

            if (mob.blockPosition().equals(behind) || d2 <= closeR * closeR) {
                mob.getNavigation().stop();
                mob.getMoveControl().setWantedPosition(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
                return;
            }

            boolean ok = mob.getNavigation().moveTo(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
            if (!ok) mob.getMoveControl().setWantedPosition(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
            return;
        }

        double clearance = frontClearanceToLip(mob, takeoffBlock, stepX, stepZ);

        if (clearance > 0.0) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);

            if (clearance > lastClearance - 1e-4) {
                if (++stallTicks >= 6) { // hard-coded stall threshold
                    double n = Math.min(0.06, Math.max(0.015, clearance * 0.25));
                    mob.push(stepX * n, 0.0, stepZ * n);
                    stallTicks = 0;
                }
            } else {
                stallTicks = 0;
            }
            lastClearance = clearance;

            double forwardSpeed = (stepX != 0 ? mob.getDeltaMovement().x * stepX
                    : mob.getDeltaMovement().z * stepZ);
            double predictedAdvance = Math.max(0.0, forwardSpeed * TICK_SECONDS);

            if (clearance > predictedAdvance + EPS) {
                return;
            }
        }

        if (!jumped && mob.onGround()) {
            mob.getNavigation().stop();
            mob.getJumpControl().jump();
            mob.setDeltaMovement(this.plannedVel);
            jumped = true;
            clearPlan();
        }
    }

    /* ---------------- helpers ---------------- */

    private static Vec3 computeEdgeSafeTakeoffPoint(Mob mob, BlockPos takeoffFeet, int sx, int sz) {
        double half = mob.getBbWidth() * 0.5;
        double cx = takeoffFeet.getX() + 0.5;
        double cz = takeoffFeet.getZ() + 0.5;

        if (sx != 0) {
            double lipX = (sx > 0) ? (takeoffFeet.getX() + 1.0) : takeoffFeet.getX();
            double x = lipX - sx * (half + EPS); // stay inside tile by half width
            return new Vec3(x, mob.getY(), cz);
        } else {
            double lipZ = (sz > 0) ? (takeoffFeet.getZ() + 1.0) : takeoffFeet.getZ();
            double z = lipZ - sz * (half + EPS);
            return new Vec3(cx, mob.getY(), z);
        }
    }

    private static double frontClearanceToLip(Mob m, BlockPos tf, int sx, int sz) {
        double half = m.getBbWidth() * 0.5;
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

    private Vec3 preferredLook() {
        LivingEntity t = mob.getTarget();
        if (t != null && t.isAlive()) return t.getEyePosition();
        if (landingBlock != null)    return Vec3.atCenterOf(landingBlock);
        if (takeoffPoint  != null)   return takeoffPoint;
        return mob.position();
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


    private static boolean isSolidTop(Level w, BlockPos posBelow) {
        var state = w.getBlockState(posBelow);
        return !state.getCollisionShape(w, posBelow).isEmpty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasHeadroom(Level w, BlockPos feet) {
        return noCollision(w, feet)
                && noCollision(w, feet.above())
                && noCollision(w, feet.above(2));
    }

    private static boolean hasClearGapCeiling(Level w, BlockPos takeoffFeet, int stepX, int stepZ, int gap) {
        for (int t = 1; t <= gap; t++) {
            BlockPos feet = takeoffFeet.offset(stepX * t, 0, stepZ * t);
            if (!hasHeadroom(w, feet)) return false;
        }
        return true;
    }

    private static boolean noCollision(Level w, BlockPos pos) {
        var state = w.getBlockState(pos);
        return state.getCollisionShape(w, pos).isEmpty();
    }

    private static Optional<Vec3> computeJumpVelocity(Mob mob, Vec3 landingCenter) {
        for (int angle = 50; angle <= 85; angle += 5) {
            Optional<Vec3> v = LongJumpUtil.calculateJumpVectorForAngle(mob, landingCenter, 1.6F, angle, true);
            if (v.isPresent()) return v;
        }
        return Optional.empty();
    }
}
