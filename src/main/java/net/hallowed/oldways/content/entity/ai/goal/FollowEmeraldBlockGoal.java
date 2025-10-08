package net.hallowed.oldways.content.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

import java.util.EnumSet;
import java.util.function.Predicate;

public class FollowEmeraldBlockGoal extends Goal {
    private final PathAwareEntity mob;
    private final double speed;
    private final double followRadius;
    private final double stopDistance;

    private PlayerEntity target;

    private boolean showingOffer = false;
    private ItemStack prevMainHand = ItemStack.EMPTY;
    private int offerRefreshCooldown = 0;

    private final Predicate<PlayerEntity> isTempting = p ->
            p.getMainHandStack().isOf(Items.EMERALD) ||
                    p.getMainHandStack().isOf(Items.EMERALD_BLOCK) ||
                    p.getOffHandStack().isOf(Items.EMERALD) ||
                    p.getOffHandStack().isOf(Items.EMERALD_BLOCK);

    public FollowEmeraldBlockGoal(PathAwareEntity mob, double speed, double followRadius, double stopDistance) {
        this.mob = mob;
        this.speed = speed;
        this.followRadius = followRadius;
        this.stopDistance = stopDistance;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        target = findTemptingPlayer();
        showingOffer = false;
        prevMainHand = ItemStack.EMPTY;
        offerRefreshCooldown = 0;
        return target != null;
    }

    @Override
    public boolean shouldContinue() {
        if (target == null || !target.isAlive()) return false;
        if (!isTempting.test(target)) return false;
        double max2 = (followRadius + 1.0) * (followRadius + 1.0);
        return target.squaredDistanceTo(mob) <= max2;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        clearOfferInHand();
        target = null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        PlayerEntity closest = findTemptingPlayer();
        if (closest != null) target = closest;
        if (target == null) { clearOfferInHand(); return; }

        mob.getLookControl().lookAt(target, 30.0F, 30.0F);
        double d2 = mob.squaredDistanceTo(target);
        if (d2 > stopDistance * stopDistance) {
            mob.getNavigation().startMovingTo(target, speed);
        } else {
            mob.getNavigation().stop();
        }

        if (mob instanceof VillagerEntity) {
            maybeShowOfferInHand((VillagerEntity) mob);
        }
    }

    private PlayerEntity findTemptingPlayer() {
        double best = followRadius * followRadius;
        PlayerEntity closest = null;
        for (PlayerEntity p : mob.getWorld().getPlayers()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (!isTempting.test(p)) continue;
            double d2 = p.squaredDistanceTo(mob);
            if (d2 <= best) {
                best = d2;
                closest = p;
            }
        }
        return closest;
    }


    private void maybeShowOfferInHand(VillagerEntity villager) {
        if (offerRefreshCooldown > 0) {
            offerRefreshCooldown--;
            return;
        }
        offerRefreshCooldown = 10;

        TradeOfferList offers = villager.getOffers();
        if (offers == null || offers.isEmpty()) {
            clearOfferInHand();
            return;
        }

        TradeOffer offer = offers.stream().filter(o -> !o.isDisabled()).findFirst().orElse(offers.getFirst());
        ItemStack sell = offer.getSellItem();
        if (sell == null || sell.isEmpty()) {
            clearOfferInHand();
            return;
        }

        ItemStack preview = sell.copy();
        preview.setCount(1);

        if (!showingOffer) {
            prevMainHand = villager.getMainHandStack().copy();
            showingOffer = true;
        }

        ItemStack current = villager.getMainHandStack();
        if (!ItemStack.areEqual(current, preview)) {
            villager.setStackInHand(Hand.MAIN_HAND, preview);
        }
    }

    private void clearOfferInHand() {
        if (showingOffer && mob instanceof VillagerEntity villager) {
            villager.setStackInHand(Hand.MAIN_HAND, prevMainHand);
        }
        showingOffer = false;
        prevMainHand = ItemStack.EMPTY;
        offerRefreshCooldown = 0;
    }
}
