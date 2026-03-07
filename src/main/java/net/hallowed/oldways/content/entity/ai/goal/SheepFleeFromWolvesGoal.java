package net.hallowed.oldways.content.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jetbrains.annotations.NotNull;

public class SheepFleeFromWolvesGoal extends AvoidEntityGoal<@NotNull Wolf> {

    public SheepFleeFromWolvesGoal(PathfinderMob mob) {
        super(
                mob,
                Wolf.class,
                12.0F,
                1.0D,
                1.0D,
                (LivingEntity e) -> (e instanceof Wolf w) && (!w.isTame() || !w.isOrderedToSit())
        );
    }

    public SheepFleeFromWolvesGoal(PathfinderMob mob,
                                   float radius,
                                   double slowSpeed,
                                   double fastSpeed,
                                   Predicate<Wolf> wolfFilter) {
        super(
                mob,
                Wolf.class,
                radius,
                slowSpeed,
                fastSpeed,
                (LivingEntity e) -> (e instanceof Wolf w) && wolfFilter.test(w)
        );
    }

    public static SheepFleeFromWolvesGoal avoidNonSitting(PathfinderMob mob) {
        return new SheepFleeFromWolvesGoal(mob, 12.0F, 1.0D, 1.0D, w -> !w.isOrderedToSit());
    }
}
