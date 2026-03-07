package net.hallowed.oldways.content.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class RunWhileChargingCrossbowGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private int repathCooldown = 0;

    public RunWhileChargingCrossbowGoal(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !mob.isAlive()) return false;

        ItemStack using = mob.getUseItem();
        if (using.isEmpty() || !(using.getItem() instanceof CrossbowItem)) return false;

        return mob.isUsingItem() && !CrossbowItem.isCharged(using);
    }

    @Override
    public boolean canContinueToUse() {
        ItemStack using = mob.getUseItem();
        return mob.isAlive()
                && mob.getTarget() != null
                && mob.isUsingItem()
                && !using.isEmpty()
                && using.getItem() instanceof CrossbowItem
                && !CrossbowItem.isCharged(using)
                && !mob.isPassenger();
    }

    @Override
    public void start() {
        repathCooldown = 0;
        if (!mob.isUsingItem()) {
            InteractionHand h = mob.getUsedItemHand();
            mob.startUsingItem(h);
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

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (repathCooldown-- <= 0) {
            repathCooldown = 8;

            Vec3 away = mob.position().subtract(target.position());
            double len = Math.hypot(away.x, away.z);
            if (len < 1.0E-4) return;
            away = new Vec3(away.x / len, 0.0, away.z / len);

            double wobble = ((mob.tickCount >> 3) & 1) == 0 ? 0.35 : -0.35;
            Vec3 tangent = new Vec3(-away.z, 0.0, away.x).scale(wobble);

            Vec3 dest = mob.position().add(away.scale(8.0)).add(tangent);
            mob.getNavigation().moveTo(dest.x, dest.y, dest.z, speed);
        }
    }
}
