package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.WolfEntity;

import java.util.function.Predicate;

public class SheepFleeFromWolvesGoal extends FleeEntityGoal<WolfEntity> {

    public SheepFleeFromWolvesGoal(PathAwareEntity mob) {
        super(
                mob,
                WolfEntity.class,
                12.0F,
                1.0D,
                1.0D,
                (LivingEntity e) -> (e instanceof WolfEntity w) && (!w.isTamed() || !w.isSitting())
        );
    }

    public SheepFleeFromWolvesGoal(PathAwareEntity mob,
                                   float radius,
                                   double slowSpeed,
                                   double fastSpeed,
                                   Predicate<WolfEntity> wolfFilter) {
        super(
                mob,
                WolfEntity.class,
                radius,
                slowSpeed,
                fastSpeed,
                (LivingEntity e) -> (e instanceof WolfEntity w) && wolfFilter.test(w)
        );
    }

    public static SheepFleeFromWolvesGoal avoidNonSitting(PathAwareEntity mob) {
        return new SheepFleeFromWolvesGoal(mob, 12.0F, 1.0D, 1.0D, w -> !w.isSitting());
    }
}
