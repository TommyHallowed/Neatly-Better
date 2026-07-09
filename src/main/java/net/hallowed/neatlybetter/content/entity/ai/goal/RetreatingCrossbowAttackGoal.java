package net.hallowed.neatlybetter.content.entity.ai.goal;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RetreatingCrossbowAttackGoal<T extends PathfinderMob & RangedAttackMob & CrossbowAttackMob> extends Goal {
    private static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private static final int RETREAT_REPATH_INTERVAL = 8;

    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final float retreatDistanceSqr;

    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;
    private int retreatPathDelay;
    private int shieldHoldoff = 0;

    public RetreatingCrossbowAttackGoal(T mob, double speedModifier, float attackRadius, float retreatDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.retreatDistanceSqr = retreatDistance * retreatDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isValidTarget() && isHoldingCrossbow();
    }

    @Override
    public boolean canContinueToUse() {
        return isValidTarget() && (canUse() || !mob.getNavigation().isDone()) && isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive();
    }

    private boolean isHoldingCrossbow() {
        return mob.isHolding(Items.CROSSBOW);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        mob.setAggressive(false);
        mob.setTarget(null);
        seeTime = 0;
        crossbowState = CrossbowState.UNCHARGED;
        updatePathDelay = 0;
        retreatPathDelay = 0;
        shieldHoldoff = 0;
        if (mob.isUsingItem()) {
            mob.stopUsingItem();
            mob.setChargingCrossbow(false);
            mob.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        boolean hasLineOfSight = mob.getSensing().hasLineOfSight(target);
        boolean hadLineOfSight = seeTime > 0;
        if (hasLineOfSight != hadLineOfSight) seeTime = 0;
        if (hasLineOfSight) seeTime++; else seeTime--;

        double distSqr = mob.distanceToSqr(target);
        boolean tooClose = distSqr < retreatDistanceSqr;
        boolean needsToMove = (distSqr > attackRadiusSqr || seeTime < 5) && attackDelay == 0;
        boolean chargingAllowed = tooClose || !needsToMove;

        if (tooClose) {
            tickRetreatMovement(target);
        } else if (needsToMove) {
            if (--updatePathDelay <= 0) {
                mob.getNavigation().moveTo(target, canRun() ? speedModifier : speedModifier * 0.5D);
                updatePathDelay = PATHFINDING_DELAY_RANGE.sample(mob.getRandom());
            }
        } else {
            updatePathDelay = 0;
            mob.getNavigation().stop();
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (crossbowState == CrossbowState.UNCHARGED) {
            if (chargingAllowed) {
                mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(mob, Items.CROSSBOW));
                crossbowState = CrossbowState.CHARGING;
                mob.setChargingCrossbow(true);
            }
        } else if (crossbowState == CrossbowState.CHARGING) {
            if (!mob.isUsingItem()) {
                crossbowState = CrossbowState.UNCHARGED;
            } else {
                int pullTime = mob.getTicksUsingItem();
                ItemStack useItem = mob.getUseItem();
                if (pullTime >= CrossbowItem.getChargeDuration(useItem, mob)) {
                    mob.releaseUsingItem();
                    crossbowState = CrossbowState.CHARGED;
                    attackDelay = 20 + mob.getRandom().nextInt(20);
                    mob.setChargingCrossbow(false);
                }
            }
        } else if (crossbowState == CrossbowState.CHARGED) {
            attackDelay--;
            if (attackDelay <= 0) {
                crossbowState = CrossbowState.READY_TO_ATTACK;
            }
        } else if (crossbowState == CrossbowState.READY_TO_ATTACK && hasLineOfSight) {
            if (!shouldHoldFireForShield(target)) {
                mob.performRangedAttack(target, 1.0F);
                crossbowState = CrossbowState.UNCHARGED;
            }
        }
    }

    private boolean shouldHoldFireForShield(LivingEntity target) {
        if (!NTServerConfig.CONFIG.rangedMobShieldHoldoff.get()) return false;

        if (mob.level().getDifficulty() != Difficulty.HARD) {
            shieldHoldoff = 0;
            return false;
        }

        if (target.isBlocking()) {
            ItemStack blockingWith = target.getItemBlockingWith();
            if (blockingWith != null && blockingWith.is(ItemTags.SWORDS)) {
                return false;
            }
            shieldHoldoff = 4;
            return true;
        }

        if (shieldHoldoff > 0) {
            shieldHoldoff--;
            return true;
        }

        return false;
    }

    private boolean canRun() {
        return crossbowState == CrossbowState.UNCHARGED;
    }

    private void tickRetreatMovement(LivingEntity target) {
        if (retreatPathDelay-- <= 0) {
            retreatPathDelay = RETREAT_REPATH_INTERVAL;

            Vec3 away = mob.position().subtract(target.position());
            double len = Math.hypot(away.x, away.z);
            if (len < 1.0E-4) return;
            away = new Vec3(away.x / len, 0.0, away.z / len);

            double wobble = ((mob.tickCount >> 3) & 1) == 0 ? 0.35 : -0.35;
            Vec3 tangent = new Vec3(-away.z, 0.0, away.x).scale(wobble);

            Vec3 dest = mob.position().add(away.scale(8.0)).add(tangent);
            mob.getNavigation().moveTo(dest.x, dest.y, dest.z, speedModifier);
        }
    }

    private enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}