package net.hallowed.neatlybetter.mixin.entity.hostile;

import net.hallowed.neatlybetter.content.entity.ai.goal.OldSpiderAttackGoal;

import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.spider.Spider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spider.class)
public abstract class SpiderMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void neatlybetter$revertSpiderAI(CallbackInfo ci) {
        Spider self = (Spider) (Object) this;

        // 1. Remove the modern, delayed leaping and sluggish melee goals
        self.goalSelector.removeAllGoals(goal ->
                goal instanceof MeleeAttackGoal || goal instanceof LeapAtTargetGoal
        );

        // 2. Inject our hyper-aggressive 1.0 Spider logic
        // We set priority to 3 to seamlessly replace where the vanilla leap/attack goals normally sit.
        self.goalSelector.addGoal(3, new OldSpiderAttackGoal(self));
    }
}