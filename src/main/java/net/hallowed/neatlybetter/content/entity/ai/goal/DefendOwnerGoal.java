package net.hallowed.neatlybetter.content.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class DefendOwnerGoal extends TargetGoal {

    private final TamableAnimal tamable;
    private int scanCooldown;

    public DefendOwnerGoal(TamableAnimal tamable) {
        super(tamable, false);
        this.tamable = tamable;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!tamable.isTame() || tamable.isOrderedToSit()) return false;

        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = adjustedTickDelay(20);

        LivingEntity owner = tamable.getOwner();
        if (owner == null) return false;

        double closestDistSq = Double.MAX_VALUE;
        Mob closestThreat = null;

        AABB area = tamable.getBoundingBox().inflate(16.0);
        for (Mob nearby : tamable.level().getEntitiesOfClass(Mob.class, area)) {
            if (nearby == tamable || !nearby.isAlive()) continue;
            LivingEntity target = nearby.getTarget();
            if (target != null && target.is(owner) && tamable.wantsToAttack(nearby, owner)) {
                double dist = tamable.distanceToSqr(nearby);
                if (dist < closestDistSq) {
                    closestDistSq = dist;
                    closestThreat = nearby;
                }
            }
        }

        if (closestThreat != null) {
            this.targetMob = closestThreat;
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        mob.setTarget(targetMob);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        scanCooldown = 0;
    }
}
