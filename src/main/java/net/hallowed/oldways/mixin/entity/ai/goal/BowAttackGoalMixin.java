package net.hallowed.oldways.mixin.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowAttackGoal.class)
public abstract class BowAttackGoalMixin {

    @Shadow @Final private HostileEntity actor;

    @Unique private int oldways$shieldHoldoff = 0;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/HostileEntity;getItemUseTime()I"
            ),
            cancellable = true
    )
    private void oldways$dontShootShields(CallbackInfo ci) {
        if (this.actor.getEntityWorld().getDifficulty() != Difficulty.HARD) {
            this.oldways$shieldHoldoff = 0;
            return;
        }

        LivingEntity target = this.actor.getTarget();
        if (target == null) return;

        if (target.isBlocking()) {
            this.oldways$shieldHoldoff = 4;
            ci.cancel();
            return;
        }

        if (this.oldways$shieldHoldoff > 0) {
            this.oldways$shieldHoldoff--;
            ci.cancel();
        }
    }
}
