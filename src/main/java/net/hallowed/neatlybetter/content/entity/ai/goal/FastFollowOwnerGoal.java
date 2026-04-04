package net.hallowed.neatlybetter.content.entity.ai.goal;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

public class FastFollowOwnerGoal extends FollowOwnerGoal {

    public FastFollowOwnerGoal(TamableAnimal tamable, double speedModifier, float startDistance, float stopDistance) {
        super(tamable, speedModifier, startDistance, stopDistance);
    }
}
