package net.hallowed.oldways.content.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class FollowEmeraldBlockGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private final double followRadius;
    private final double stopDistance;

    private Player target;

    private boolean showingOffer = false;
    private ItemStack prevMainHand = ItemStack.EMPTY;
    private int offerRefreshCooldown = 0;

    private final Predicate<Player> isTempting = p ->
            p.getMainHandItem().is(Items.EMERALD) ||
                    p.getMainHandItem().is(Items.EMERALD_BLOCK) ||
                    p.getOffhandItem().is(Items.EMERALD) ||
                    p.getOffhandItem().is(Items.EMERALD_BLOCK);

    public FollowEmeraldBlockGoal(PathfinderMob mob, double speed, double followRadius, double stopDistance) {
        this.mob = mob;
        this.speed = speed;
        this.followRadius = followRadius;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = findTemptingPlayer();
        showingOffer = false;
        prevMainHand = ItemStack.EMPTY;
        offerRefreshCooldown = 0;
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) return false;
        if (!isTempting.test(target)) return false;
        double max2 = (followRadius + 1.0) * (followRadius + 1.0);
        return target.distanceToSqr(mob) <= max2;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        clearOfferInHand();
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Player closest = findTemptingPlayer();
        if (closest != null) target = closest;
        if (target == null) { clearOfferInHand(); return; }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double d2 = mob.distanceToSqr(target);
        if (d2 > stopDistance * stopDistance) {
            mob.getNavigation().moveTo(target, speed);
        } else {
            mob.getNavigation().stop();
        }

        if (mob instanceof Villager) {
            maybeShowOfferInHand((Villager) mob);
        }
    }

    private Player findTemptingPlayer() {
        double best = followRadius * followRadius;
        Player closest = null;
        for (Player p : mob.level().players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (!isTempting.test(p)) continue;
            double d2 = p.distanceToSqr(mob);
            if (d2 <= best) {
                best = d2;
                closest = p;
            }
        }
        return closest;
    }


    private void maybeShowOfferInHand(Villager villager) {
        if (offerRefreshCooldown > 0) {
            offerRefreshCooldown--;
            return;
        }
        offerRefreshCooldown = 10;

        MerchantOffers offers = villager.getOffers();
        if (offers.isEmpty()) {
            clearOfferInHand();
            return;
        }

        MerchantOffer offer = offers.stream().filter(o -> !o.isOutOfStock()).findFirst().orElse(offers.getFirst());
        ItemStack sell = offer.getResult();
        if (sell.isEmpty()) {
            clearOfferInHand();
            return;
        }

        ItemStack preview = sell.copy();
        preview.setCount(1);

        if (!showingOffer) {
            prevMainHand = villager.getMainHandItem().copy();
            showingOffer = true;
        }

        ItemStack current = villager.getMainHandItem();
        if (!ItemStack.matches(current, preview)) {
            villager.setItemInHand(InteractionHand.MAIN_HAND, preview);
        }
    }

    private void clearOfferInHand() {
        if (showingOffer && mob instanceof Villager villager) {
            villager.setItemInHand(InteractionHand.MAIN_HAND, prevMainHand);
        }
        showingOffer = false;
        prevMainHand = ItemStack.EMPTY;
        offerRefreshCooldown = 0;
    }
}
