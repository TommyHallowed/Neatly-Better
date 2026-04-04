package net.hallowed.neatlybetter.content.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class TamedWolfMeleeAttackGoal extends MeleeAttackGoal {

    public TamedWolfMeleeAttackGoal(Wolf mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    protected int getAttackInterval() {
        return adjustedTickDelay(10);
    }
}
