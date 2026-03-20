package net.hallowed.neatlybetter.mixin.entity.ai.goal;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwellGoal.class)
public abstract class SwellGoalMixin {

    @Final
    @Shadow private Creeper creeper;
    @Shadow @Nullable private LivingEntity target;

    @Unique private boolean neatlybetter$hasBeenSeen;
    @Unique private int neatlybetter$orbitDir;
    @Unique private double neatlybetter$theta;
    @Unique private int neatlybetter$nextRepathTick;

    @Inject(method = "start", at = @At("TAIL"))
    private void neatlybetter$onStart(CallbackInfo ci) {
        if (!neatlybetter$anyFeatureEnabled()) return;

        this.neatlybetter$hasBeenSeen = false;
        this.neatlybetter$orbitDir = this.creeper.getRandom().nextBoolean() ? 1 : -1;

        if (this.target != null) {
            double dx = this.creeper.getX() - this.target.getX();
            double dz = this.creeper.getZ() - this.target.getZ();
            this.neatlybetter$theta = Math.atan2(dz, dx);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$onTick(CallbackInfo ci) {
        if (!neatlybetter$anyFeatureEnabled()) return;

        if (this.target == null) {
            this.creeper.setSwellDir(-1);
            ci.cancel();
            return;
        }

        if (this.target instanceof Player player) {
            // 1. STEALTH CHECK — gated by creeperSneaky
            if (NTServerConfig.CONFIG.creeperSneaky.get() && !this.neatlybetter$hasBeenSeen) {
                Vec3 viewVector = player.getViewVector(1.0F).normalize();
                Vec3 toCreeper = this.creeper.position().subtract(player.position()).normalize();

                if (viewVector.dot(toCreeper) > 0.3) {
                    this.neatlybetter$hasBeenSeen = true;
                }
            } else {
                this.neatlybetter$hasBeenSeen = true;
            }

            // 2. SNEAKING LOGIC (Unseen) — gated by creeperSneaky
            if (NTServerConfig.CONFIG.creeperSneaky.get() && !this.neatlybetter$hasBeenSeen) {
                if (this.creeper.tickCount >= this.neatlybetter$nextRepathTick) {
                    this.creeper.getNavigation().moveTo(player, 1.2D);
                    this.neatlybetter$nextRepathTick = this.creeper.tickCount + 5;
                }
                this.creeper.getLookControl().setLookAt(player, 30.0F, 30.0F);

                this.creeper.setSwellDir(-1);
                ci.cancel();
                return;
            }

            // 3. MOVEMENT LOGIC (Spotted)
            Difficulty diff = this.creeper.level().getDifficulty();

            if (diff == Difficulty.HARD && NTServerConfig.CONFIG.creeperOrbiting.get()) {
                // Orbiting Logic — gated by creeperOrbiting
                final double MIN_RADIUS = 0.2;
                final double PREF_RADIUS = 1.4;
                final double ANG_SPEED = 0.16;

                this.neatlybetter$theta += ANG_SPEED * this.neatlybetter$orbitDir;

                double cx = player.getX();
                double cz = player.getZ();
                double dx = this.creeper.getX() - cx;
                double dz = this.creeper.getZ() - cz;
                double dist = Math.sqrt(dx * dx + dz * dz);

                double r = Math.max(MIN_RADIUS, Math.min(PREF_RADIUS, dist));
                double tx = cx + Math.cos(this.neatlybetter$theta) * r;
                double tz = cz + Math.sin(this.neatlybetter$theta) * r;

                if (this.creeper.tickCount >= this.neatlybetter$nextRepathTick) {
                    this.creeper.getNavigation().moveTo(tx, player.getY(), tz, 1.2D);
                    this.neatlybetter$nextRepathTick = this.creeper.tickCount + 5;
                }
                this.creeper.getLookControl().setLookAt(player, 30.0F, 30.0F);

            } else if (diff == Difficulty.NORMAL && NTServerConfig.CONFIG.creeperWalkIgnite.get()) {
                // Walk-toward Logic — gated by creeperWalkIgnite
                if (this.creeper.tickCount >= this.neatlybetter$nextRepathTick) {
                    this.creeper.getNavigation().moveTo(player, 1.2D);
                    this.neatlybetter$nextRepathTick = this.creeper.tickCount + 5;
                }
            }
        } else {
            // Fallback for non-player targets (golems, etc.)
            if (this.creeper.tickCount >= this.neatlybetter$nextRepathTick) {
                this.creeper.getNavigation().moveTo(this.target, 1.2D);
                this.neatlybetter$nextRepathTick = this.creeper.tickCount + 5;
            }
        }

        // 4. IGNITION LOGIC
        if (this.creeper.distanceToSqr(this.target) > 49.0D) {
            this.creeper.setSwellDir(-1);
        } else if (!this.creeper.getSensing().hasLineOfSight(this.target)) {
            this.creeper.setSwellDir(-1);
        } else {
            this.creeper.setSwellDir(1);
        }

        ci.cancel();
    }

    @Unique
    private static boolean neatlybetter$anyFeatureEnabled() {
        return NTServerConfig.CONFIG.creeperOrbiting.get()
                || NTServerConfig.CONFIG.creeperWalkIgnite.get()
                || NTServerConfig.CONFIG.creeperSneaky.get();
    }
}