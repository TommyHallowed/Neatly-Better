package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.WolfEntity;

import java.util.function.Predicate;

/** Sheep flee from wolves. Ignores tamed wolves that are sitting. */
public class SheepFleeFromWolvesGoal extends FleeEntityGoal<WolfEntity> {

    /** Default: radius 12, speed 1.0/1.0, flee if wolf is untamed OR (tamed and not sitting). */
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

    /** Customizable constructor with your own predicate on the WolfEntity. */
    public SheepFleeFromWolvesGoal(PathAwareEntity mob,
                                   float radius,
                                   double slowSpeed,
                                   double fastSpeed,
                                   Predicate<WolfEntity> wolfFilter) {
        // FleeEntityGoal<T> expects a Predicate<LivingEntity>, so adapt the WolfEntity predicate.
        super(
                mob,
                WolfEntity.class,
                radius,
                slowSpeed,
                fastSpeed,
                (LivingEntity e) -> (e instanceof WolfEntity w) && wolfFilter.test(w)
        );
    }

    /** Convenience factory: flee from any wolf that isn't sitting (tamed sitting is ignored). */
    public static SheepFleeFromWolvesGoal avoidNonSitting(PathAwareEntity mob) {
        return new SheepFleeFromWolvesGoal(mob, 12.0F, 1.0D, 1.0D, w -> !w.isSitting());
    }
}
