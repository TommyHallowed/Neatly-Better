package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class OldSpiderAttackGoal extends Goal {
    private final Spider spider;
    private int attackTime;
    private int pathUpdateDelay;

    public OldSpiderAttackGoal(Spider spider) {
        this.spider = spider;
        // Require movement and looking, just like vanilla melee goals
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.spider.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.spider.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Minecraft 1.0 daylight logic: 1% chance per tick to drop target if in bright light
        float brightness = this.spider.getLightLevelDependentMagicValue();
        if (brightness >= 0.5F && this.spider.getRandom().nextInt(100) == 0) {
            this.spider.setTarget(null);
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.attackTime = 0;
        this.pathUpdateDelay = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.spider.getTarget();
        if (target == null) return;

        // Face the target
        this.spider.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Responsive pathfinding: Old MC didn't hesitate when you moved.
        // We update the path every 4 ticks (very fast) to prevent sluggishness without crashing the server.
        if (--this.pathUpdateDelay <= 0) {
            this.spider.getNavigation().moveTo(target, 1.0D); // Base speed
            this.pathUpdateDelay = 4;
        }

        if (this.attackTime > 0) {
            this.attackTime--;
        }

        // Distance is center-to-center (exactly how old MC `getDistanceToEntity` worked)
        double distanceToTarget = this.spider.distanceTo(target);

        // MINECRAFT 1.0 LEAP LOGIC: Distance 2 to 6 blocks, 10% chance per tick to jump!
        if (distanceToTarget > 2.0F && distanceToTarget < 6.0F && this.spider.getRandom().nextInt(10) == 0) {
            if (this.spider.onGround()) {
                double dX = target.getX() - this.spider.getX();
                double dZ = target.getZ() - this.spider.getZ();
                float f = Mth.sqrt((float) (dX * dX + dZ * dZ));

                // Exact 1.0 math: this.motionX = var4 / (double)var8 * 0.5D * (double)0.8F + this.motionX * (double)0.2F;
                Vec3 currentMotion = this.spider.getDeltaMovement();
                double motionX = (dX / f) * 0.5D * 0.8D + currentMotion.x * 0.2D;
                double motionZ = (dZ / f) * 0.5D * 0.8D + currentMotion.z * 0.2D;
                double motionY = 0.4D;

                this.spider.setDeltaMovement(motionX, motionY, motionZ);
            }
        }
        // MINECRAFT 1.0 ATTACK LOGIC: Distance < 2 blocks, instant attack!
        else if (distanceToTarget <= 2.0F) {
            // Check Y bounding box overlap and cooldown
            if (this.attackTime <= 0
                    && target.getBoundingBox().maxY > this.spider.getBoundingBox().minY
                    && target.getBoundingBox().minY < this.spider.getBoundingBox().maxY) {

                this.attackTime = 20; // Old MC attack cooldown was 20 ticks

                // Attack the target! (1.21.4 requires the ServerLevel cast)
                if (!this.spider.level().isClientSide()) {
                    this.spider.doHurtTarget((ServerLevel) this.spider.level(), target);
                }
            }
        }
    }
}