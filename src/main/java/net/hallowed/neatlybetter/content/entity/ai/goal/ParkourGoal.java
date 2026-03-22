package net.hallowed.neatlybetter.content.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ParkourGoal extends Goal {

    private static final int MAX_GAP = 2;

    private static final double JUMP_VY         = 0.42;
    private static final double MC_GRAVITY      = 0.08;
    private static final double VERTICAL_DRAG   = 0.98;
    private static final double HORIZONTAL_DRAG = 0.91;

    private static final double EPS                = 1e-3;
    private static final int    MAX_SIM_TICKS      = 40;
    private static final int    MAX_APPROACH_TICKS = 80;
    private static final int    COOLDOWN_TICKS     = 20;
    private static final double LANDING_OVERSHOOT  = 0.20;
    private static final double HAZARD_LANDING_OVERSHOOT = 0.35;
    private static final double MAX_HORIZONTAL_VEL = 1.2;
    private static final int    MAX_AIR_TICKS      = 40;
    private static final int    MIN_AIR_TICKS      = 3;
    private static final int    BRAKE_TICKS        = 4;
    private static final double HAZARD_CLEARANCE   = 0.25;

    private static final int EVAL_INTERVAL = 4;

    private static final int PRECOMPUTED_LAND_TICK;

    private static final int PRECOMPUTED_TARGET_TICK;

    private static final double PRECOMPUTED_HORIZ_SUM;

    static {

        double vy = JUMP_VY;
        double y  = 0;
        int landTick = -1;

        for (int t = 1; t <= MAX_SIM_TICKS; t++) {
            y  += vy;
            vy -= MC_GRAVITY;
            vy *= VERTICAL_DRAG;
            if (y <= 0 && t > 2) {
                landTick = t;
                break;
            }
        }
        PRECOMPUTED_LAND_TICK = landTick;
        PRECOMPUTED_TARGET_TICK = Math.max(3, landTick - 1);

        double sum  = 0;
        double drag = 1.0;
        for (int i = 0; i < PRECOMPUTED_TARGET_TICK; i++) {
            sum  += drag;
            drag *= HORIZONTAL_DRAG;
        }
        PRECOMPUTED_HORIZ_SUM = sum;
    }

    private final Mob mob;

    private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    private BlockPos takeoffBlock;
    private Vec3     takeoffPoint;
    private BlockPos landingBlock;
    private Vec3     plannedVel;
    private int      stepX, stepZ;
    private int      gapSize;

    private boolean jumped;
    private boolean landed;
    private int     airTicks;
    private int     brakeTicks;
    private double  lastClearance;
    private int     stallTicks;
    private int     approachTicks;
    private int     cooldown;

    private int     evalCountdown;

    public ParkourGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {

        if (mob.level().getDifficulty() != Difficulty.HARD)  return false;
        if (!mob.onGround() || !mob.isAlive())               return false;
        if (mob.isPassenger() || mob.isVehicle())            return false;
        if (mob.isInWater() || mob.isInLava())               return false;
        if (cooldown > 0) { cooldown--; return false; }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (--evalCountdown > 0) return false;
        evalCountdown = EVAL_INTERVAL;

        computeDirection(target);

        Level level        = mob.level();
        BlockPos feet      = mob.blockPosition();
        BlockPos belowFeet = feet.below();

        if (!isSolidTop(level, belowFeet) || !hasHeadroom(level, feet)) return false;

        return scanAndPlan(level, belowFeet);
    }

    @Override
    public boolean canContinueToUse() {
        if (!mob.isAlive()) return false;
        if (jumped) return !landed;
        return plannedVel != null && approachTicks < MAX_APPROACH_TICKS;
    }

    @Override
    public void start() {
        approachTicks = 0;
    }

    @Override
    public void stop() {
        if (!landed) {
            cooldown = COOLDOWN_TICKS;
        }
        clearPlan();
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!canContinueToUse()) return;

        if (jumped) {

            if (airTicks < MIN_AIR_TICKS) {
                airTicks++;
                return;
            }

            if (!mob.onGround()) {
                if (++airTicks > MAX_AIR_TICKS) {
                    landed = true;
                }
                return;
            }

            mob.getNavigation().stop();
            double cx = landingBlock.getX() + 0.5;
            double cz = landingBlock.getZ() + 0.5;
            double pullX = (cx - mob.getX()) * 0.25;
            double pullZ = (cz - mob.getZ()) * 0.25;
            mob.setDeltaMovement(pullX, mob.getDeltaMovement().y, pullZ);

            if (++brakeTicks < BRAKE_TICKS) {
                return;
            }

            LivingEntity target = mob.getTarget();
            if (target != null && target.isAlive()) {
                computeDirection(target);
                BlockPos feet      = mob.blockPosition();
                BlockPos belowFeet = feet.below();

                if (isSolidTop(mob.level(), belowFeet)
                        && hasHeadroom(mob.level(), feet)
                        && scanAndPlan(mob.level(), belowFeet)) {
                    jumped        = false;
                    airTicks      = 0;
                    brakeTicks    = 0;
                    approachTicks = 0;
                    lastClearance = Double.POSITIVE_INFINITY;
                    stallTicks    = 0;
                    return;
                }
            }

            landed = true;
            return;
        }

        approachTicks++;

        Vec3 look = preferredLook();
        mob.getLookControl().setLookAt(look.x, look.y, look.z);

        if (!mob.blockPosition().equals(takeoffBlock)) {
            navigateToTakeoff();
            return;
        }

        double clearance = frontClearanceToLip();
        if (clearance > 0.0) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(
                    takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);

            if (clearance > lastClearance - 1e-4) {
                if (++stallTicks >= 5) {
                    double nudge = Mth.clamp(clearance * 0.3, 0.01, 0.05);
                    mob.push(stepX * nudge, 0.0, stepZ * nudge);
                    stallTicks = 0;
                }
            } else {
                stallTicks = 0;
            }
            lastClearance = clearance;

            double forwardSpeed = stepX != 0
                    ? mob.getDeltaMovement().x * stepX
                    : mob.getDeltaMovement().z * stepZ;
            if (clearance > Math.max(0.0, forwardSpeed) + EPS) {
                return;
            }
        }

        if (!jumped && mob.onGround()) {
            mob.getNavigation().stop();
            mob.getJumpControl().jump();
            mob.setDeltaMovement(plannedVel);
            jumped = true;
            airTicks = 0;
            brakeTicks = 0;
        }
    }

    private boolean scanAndPlan(Level level, BlockPos belowFeet) {
        int gap = 0;
        boolean gapHazardous = false;
        BlockPos edgeBelow = null;

        for (int i = 1; i <= MAX_GAP + 2; i++) {

            mutable.set(
                    belowFeet.getX() + stepX * i,
                    belowFeet.getY(),
                    belowFeet.getZ() + stepZ * i);

            if (gap == 0) {
                if (!isSolidTop(level, mutable)) {
                    if (isColumnHazardous(level, mutable)) gapHazardous = true;
                    gap = 1;

                    edgeBelow = belowFeet.offset(stepX * (i - 1), 0, stepZ * (i - 1));
                }
            } else {
                if (!isSolidTop(level, mutable)) {
                    if (isColumnHazardous(level, mutable)) gapHazardous = true;
                    if (++gap > MAX_GAP) return false;
                } else {

                    return buildJumpPlan(level, edgeBelow, mutable.immutable(), gap, gapHazardous);
                }
            }
        }
        return false;
    }

    private boolean buildJumpPlan(Level level, BlockPos edgeBelow,
                                  BlockPos landingSurface, int gap,
                                  boolean gapHazardous) {
        BlockPos landFeet    = landingSurface.above();
        BlockPos takeoffFeet = edgeBelow.above();

        if (!hasHeadroom(level, takeoffFeet)) return false;
        if (!hasHeadroom(level, landFeet))    return false;
        if (!hasClearGapCeiling(level, takeoffFeet, gap)) return false;

        if (isHazardous(level, landingSurface))  return false;
        if (isHazardous(level, landFeet))        return false;
        if (isHazardous(level, landFeet.above())) return false;

        double overshoot = gapHazardous ? HAZARD_LANDING_OVERSHOOT : LANDING_OVERSHOOT;

        Vec3 takeoff = computeEdgeTakeoffPoint(takeoffFeet);
        Vec3 landTarget = new Vec3(
                landFeet.getX() + 0.5 + stepX * overshoot,
                landFeet.getY(),
                landFeet.getZ() + 0.5 + stepZ * overshoot);

        double horizDist = Math.abs(stepX != 0
                ? landTarget.x - takeoff.x
                : landTarget.z - takeoff.z);

        Vec3 vel = solveJumpVelocity(horizDist);
        if (vel == null) return false;

        if (!isTrajectoryClean(level, takeoff, vel, landFeet)) return false;

        this.takeoffBlock  = takeoffFeet;
        this.takeoffPoint  = takeoff;
        this.landingBlock  = landFeet;
        this.plannedVel    = vel;
        this.gapSize       = gap;
        this.jumped        = false;
        this.landed        = false;
        this.lastClearance = Double.POSITIVE_INFINITY;
        this.stallTicks    = 0;
        return true;
    }

    private Vec3 solveJumpVelocity(double horizDist) {
        if (PRECOMPUTED_LAND_TICK < 0) return null;

        double vxNeeded = horizDist / PRECOMPUTED_HORIZ_SUM;
        if (vxNeeded > MAX_HORIZONTAL_VEL || vxNeeded < 0.01) return null;

        return new Vec3(stepX * vxNeeded, JUMP_VY, stepZ * vxNeeded);
    }

    private boolean isTrajectoryClean(Level level, Vec3 start, Vec3 vel,
                                      BlockPos landFeet) {
        double x = start.x, y = start.y, z = start.z;
        double vx = vel.x, vy = vel.y, vz = vel.z;

        int prevBX = BlockPos.getX(0xFFFFFFF);
        int prevBY = BlockPos.getY(0xFFFFFFF);
        int prevBZ = BlockPos.getZ(0xFFFFFFF);

        for (int t = 1; t <= MAX_SIM_TICKS; t++) {
            x  += vx;
            y  += vy;
            z  += vz;
            vy -= MC_GRAVITY;
            vy *= VERTICAL_DRAG;
            vx *= HORIZONTAL_DRAG;
            vz *= HORIZONTAL_DRAG;

            int bx = Mth.floor(x);
            int by = Mth.floor(y);
            int bz = Mth.floor(z);

            if (bx == landFeet.getX()
                    && bz == landFeet.getZ()
                    && by >= landFeet.getY() - 1
                    && by <= landFeet.getY() + 2) {
                return true;
            }

            boolean newBlock = (bx != prevBX || by != prevBY || bz != prevBZ);
            if (newBlock) {
                mutable.set(bx, by, bz);

                if (!mutable.equals(takeoffBlock) && !noCollision(level, mutable)) {
                    return false;
                }

                if (isTrappingHazard(level, mutable)) return false;

                mutable.set(bx, by - 1, bz);
                if (isTrappingHazard(level, mutable)) {
                    double blockTop = (by - 1) + 1.0;
                    if (y - blockTop < HAZARD_CLEARANCE) {
                        return false;
                    }
                }

                prevBX = bx;
                prevBY = by;
                prevBZ = bz;
            }

            if (y < start.y - 4) return false;
        }
        return false;
    }

    private void computeDirection(LivingEntity target) {
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();
        if (Math.abs(dx) >= Math.abs(dz)) {
            stepX = dx >= 0 ? 1 : -1;
            stepZ = 0;
        } else {
            stepX = 0;
            stepZ = dz >= 0 ? 1 : -1;
        }
    }

    private void navigateToTakeoff() {
        double closeR = 1.0 + mob.getBbWidth();
        double d2 = mob.distanceToSqr(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z);

        if (d2 <= closeR * closeR) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(
                    takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
            return;
        }

        boolean ok = mob.getNavigation()
                .moveTo(takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
        if (!ok) {
            mob.getMoveControl().setWantedPosition(
                    takeoffPoint.x, takeoffPoint.y, takeoffPoint.z, 1.0);
        }
    }

    private Vec3 computeEdgeTakeoffPoint(BlockPos takeoffFeet) {
        double half = mob.getBbWidth() * 0.5;
        double cx   = takeoffFeet.getX() + 0.5;
        double cz   = takeoffFeet.getZ() + 0.5;

        if (stepX != 0) {
            double lip = (stepX > 0) ? (takeoffFeet.getX() + 1.0) : takeoffFeet.getX();
            return new Vec3(lip - stepX * (half + EPS), mob.getY(), cz);
        } else {
            double lip = (stepZ > 0) ? (takeoffFeet.getZ() + 1.0) : takeoffFeet.getZ();
            return new Vec3(cx, mob.getY(), lip - stepZ * (half + EPS));
        }
    }

    private double frontClearanceToLip() {
        double half = mob.getBbWidth() * 0.5;
        if (stepX != 0) {
            double front = mob.getX() + stepX * half;
            double lip   = (stepX > 0) ? (takeoffBlock.getX() + 1.0) : takeoffBlock.getX();
            return (lip - front) * stepX;
        } else {
            double front = mob.getZ() + stepZ * half;
            double lip   = (stepZ > 0) ? (takeoffBlock.getZ() + 1.0) : takeoffBlock.getZ();
            return (lip - front) * stepZ;
        }
    }

    private Vec3 preferredLook() {
        LivingEntity t = mob.getTarget();
        if (t != null && t.isAlive()) return t.getEyePosition();
        if (landingBlock != null)     return Vec3.atCenterOf(landingBlock);
        if (takeoffPoint != null)     return takeoffPoint;
        return mob.position();
    }

    private void clearPlan() {
        takeoffBlock  = null;
        takeoffPoint  = null;
        landingBlock  = null;
        plannedVel    = null;
        lastClearance = Double.POSITIVE_INFINITY;
        stallTicks    = 0;
        gapSize       = 0;
        jumped        = false;
        landed        = false;
        airTicks      = 0;
        brakeTicks    = 0;
    }

    private static boolean isTrappingHazard(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.LAVA)             return true;
        if (!level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos).is(FluidTags.LAVA)) return true;
        if (block == Blocks.CACTUS)           return true;
        if (block == Blocks.SWEET_BERRY_BUSH) return true;
        if (block == Blocks.POWDER_SNOW)      return true;
        if (block == Blocks.COBWEB)           return true;

        return false;
    }

    private boolean isHazardous(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.LAVA)             return true;
        if (!level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos).is(FluidTags.LAVA)) return true;
        if (block == Blocks.CACTUS)           return true;
        if (block == Blocks.SWEET_BERRY_BUSH) return true;
        if (block == Blocks.POWDER_SNOW)      return true;
        if (block == Blocks.WITHER_ROSE)      return true;

        if (!mob.fireImmune()) {
            if (block == Blocks.FIRE)          return true;
            if (block == Blocks.SOUL_FIRE)     return true;
            if (block == Blocks.MAGMA_BLOCK)   return true;
            if (block == Blocks.CAMPFIRE)      return true;
            if (block == Blocks.SOUL_CAMPFIRE) return true;
        }

        return false;
    }

    private boolean isColumnHazardous(Level level, BlockPos pos) {
        if (isHazardous(level, pos)) return true;
        mutable.set(pos.getX(), pos.getY() - 1, pos.getZ());
        if (isHazardous(level, mutable)) return true;
        mutable.set(pos.getX(), pos.getY() + 1, pos.getZ());
        return isHazardous(level, mutable);
    }

    private static boolean isSolidTop(Level level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasHeadroom(Level level, BlockPos feet) {
        return noCollision(level, feet)
                && noCollision(level, feet.above())
                && noCollision(level, feet.above(2));
    }

    private boolean hasClearGapCeiling(Level level, BlockPos takeoffFeet, int gap) {
        for (int t = 1; t <= gap; t++) {
            mutable.set(
                    takeoffFeet.getX() + stepX * t,
                    takeoffFeet.getY(),
                    takeoffFeet.getZ() + stepZ * t);
            if (!hasHeadroom(level, mutable)) return false;
        }
        return true;
    }

    private static boolean noCollision(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}
