package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class RunWhileChargingCrossbowGoal extends Goal {
    private final PathAwareEntity mob;
    private final double speed;
    private int repathCooldown = 0;

    public RunWhileChargingCrossbowGoal(PathAwareEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = mob.getTarget();
        if (target == null || !mob.isAlive()) return false;

        ItemStack using = mob.getActiveItem();
        if (using.isEmpty() || !(using.getItem() instanceof CrossbowItem)) return false;

        return mob.isUsingItem() && !CrossbowItem.isCharged(using);
    }

    @Override
    public boolean shouldContinue() {
        ItemStack using = mob.getActiveItem();
        return mob.isAlive()
                && mob.getTarget() != null
                && mob.isUsingItem()
                && !using.isEmpty()
                && using.getItem() instanceof CrossbowItem
                && !CrossbowItem.isCharged(using)
                && !mob.hasVehicle();
    }

    @Override
    public void start() {
        repathCooldown = 0;
        if (!mob.isUsingItem()) {
            Hand h = mob.getActiveHand();
            if (h == null) h = Hand.MAIN_HAND;
            mob.setCurrentHand(h);
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        mob.getLookControl().lookAt(target, 30.0F, 30.0F);

        if (repathCooldown-- <= 0) {
            repathCooldown = 8;

            Vec3d away = mob.getEntityPos().subtract(target.getEntityPos());
            double len = Math.hypot(away.x, away.z);
            if (len < 1.0E-4) return;
            away = new Vec3d(away.x / len, 0.0, away.z / len);

            double wobble = ((mob.age >> 3) & 1) == 0 ? 0.35 : -0.35;
            Vec3d tangent = new Vec3d(-away.z, 0.0, away.x).multiply(wobble);

            Vec3d dest = mob.getEntityPos().add(away.multiply(8.0)).add(tangent);
            mob.getNavigation().startMovingTo(dest.x, dest.y, dest.z, speed);
        }
    }
}
