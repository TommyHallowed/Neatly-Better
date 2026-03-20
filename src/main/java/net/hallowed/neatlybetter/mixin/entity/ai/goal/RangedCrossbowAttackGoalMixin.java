package net.hallowed.neatlybetter.mixin.entity.ai.goal;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedCrossbowAttackGoal.class)
public abstract class CrossbowAttackGoalMixin {

    @Shadow @Final private Monster mob;

    @Unique
    private int neatlybetter$shieldHoldoff = 0;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
            ),
            cancellable = true
    )
    private void neatlybetter$dontShootShields(CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.rangedMobShieldHoldoff.get()) return;
        if (this.mob.level().getDifficulty() != Difficulty.HARD) {
            this.neatlybetter$shieldHoldoff = 0;
            return;
        }

        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        if (target.isBlocking()) {
            this.neatlybetter$shieldHoldoff = 4;
            ci.cancel();
            return;
        }

        if (this.neatlybetter$shieldHoldoff > 0) {
            this.neatlybetter$shieldHoldoff--;
            ci.cancel();
        }
    }
}
