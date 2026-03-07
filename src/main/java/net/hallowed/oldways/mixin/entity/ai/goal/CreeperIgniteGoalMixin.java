package net.hallowed.oldways.mixin.entity.ai.goal;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwellGoal.class)
public abstract class CreeperIgniteGoalMixin {

    @Final
    @Shadow private Creeper creeper;
    @Shadow @Nullable private LivingEntity target;

    @Unique private int oldways$orbitDir;

    @Unique private double oldways$theta;
    @Unique private int oldways$nextRepathTick;
    @Unique private double oldways$lastTx, oldways$lastTz;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/monster/Creeper;)V", at = @At("TAIL"))
    private void oldways$seed(Creeper creeper, CallbackInfo ci) {
        this.oldways$orbitDir = (creeper.getId() & 1) == 0 ? 1 : -1;
        this.oldways$theta = creeper.getRandom().nextDouble() * Math.PI * 2.0;
        this.oldways$nextRepathTick = 0;
        this.oldways$lastTx = creeper.getX();
        this.oldways$lastTz = creeper.getZ();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void oldways$igniteBehavior(CallbackInfo ci) {
        if (!this.creeper.isAlive() || this.creeper.getSwellDir() <= 0) return;

        Player player = this.target instanceof Player p
                ? p
                : this.creeper.level().getNearestPlayer(this.creeper, 12.0);
        if (player == null) return;
        if (!this.creeper.getSensing().hasLineOfSight(player)) return;

        final Difficulty diff = this.creeper.level().getDifficulty();

        if (diff == Difficulty.NORMAL) {
            if (this.creeper.tickCount >= this.oldways$nextRepathTick) {
                this.creeper.getNavigation().moveTo(player, 0.80);
                this.oldways$nextRepathTick = this.creeper.tickCount + 8;
            }
            this.creeper.getLookControl().setLookAt(player, 30.0F, 30.0F);
            return;
        }

        if (diff == Difficulty.HARD) {
            final double MIN_RADIUS = 1.2;
            final double PREF_RADIUS = 2.4;
            final double ANG_SPEED   = 0.16;

            this.oldways$theta += ANG_SPEED * this.oldways$orbitDir;

            final double cx = player.getX();
            final double cz = player.getZ();
            final double dx = this.creeper.getX() - cx;
            final double dz = this.creeper.getZ() - cz;
            final double dist = Math.sqrt(dx * dx + dz * dz);

            final double r = Math.max(MIN_RADIUS, Math.min(PREF_RADIUS, dist));

            final double tx = cx + Math.cos(this.oldways$theta) * r;
            final double tz = cz + Math.sin(this.oldways$theta) * r;
            final double ty = player.getY();

            if (this.creeper.tickCount >= this.oldways$nextRepathTick ||
                    this.creeper.distanceToSqr(this.oldways$lastTx, this.creeper.getY(), this.oldways$lastTz) < 0.7 * 0.7) {
                this.creeper.getNavigation().moveTo(tx, ty, tz, 1.05);
                this.oldways$lastTx = tx;
                this.oldways$lastTz = tz;
                this.oldways$nextRepathTick = this.creeper.tickCount + 5;
            }

            this.creeper.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
    }
}
