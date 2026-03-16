package net.hallowed.neatlybetter.content.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import org.jspecify.annotations.Nullable;

public class GroundItemBreedGoal extends Goal {

    private final Animal animal;
    private @Nullable ItemEntity targetItem;
    private int searchCooldown;

    public GroundItemBreedGoal(Animal animal) {
        this.animal = animal;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (--searchCooldown > 0) return false;
        searchCooldown = 10;

        if (animal.getAge() != 0) return false;
        if (!animal.canFallInLove()) return false;
        if (animal.isPanicking()) return false;

        AABB box = animal.getBoundingBox().inflate(5);
        List<ItemEntity> items = animal.level().getEntitiesOfClass(
                ItemEntity.class, box,
                ie -> !ie.isRemoved()
                        && !ie.hasPickUpDelay()
                        && animal.isFood(ie.getItem())
        );
        if (items.isEmpty()) return false;

        ItemEntity nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (ItemEntity ie : items) {
            double d = animal.distanceToSqr(ie);
            if (d < bestDist) {
                bestDist = d;
                nearest = ie;
            }
        }

        targetItem = nearest;
        return targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetItem == null || targetItem.isRemoved()) return false;
        if (!animal.isFood(targetItem.getItem())) return false;
        if (animal.getAge() != 0) return false;
        if (!animal.canFallInLove()) return false;
        return !animal.isPanicking();
    }

    @Override
    public void start() {
        if (targetItem != null) {
            animal.getNavigation().moveTo(targetItem, 1.0);
        }
    }

    @Override
    public void tick() {
        if (targetItem == null || targetItem.isRemoved()) return;

        animal.getLookControl().setLookAt(
                targetItem, 10.0F, (float) animal.getMaxHeadXRot());
        animal.getNavigation().moveTo(targetItem, 1.0);

        if (animal.distanceToSqr(targetItem) < 2.25) {
            consumeOneItem();
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        animal.getNavigation().stop();
    }

    private void consumeOneItem() {
        if (targetItem == null || targetItem.isRemoved()) return;

        ItemStack stack = targetItem.getItem();
        if (stack.isEmpty()) return;

        if (animal.getAge() != 0 || !animal.canFallInLove()) return;

        Player thrower = null;
        Entity owner = targetItem.getOwner();
        if (owner instanceof Player p) {
            thrower = p;
        }

        if (stack.getCount() <= 1) {
            targetItem.discard();
        } else {
            targetItem.setItem(stack.copyWithCount(stack.getCount() - 1));
        }

        animal.setInLove(thrower);
        targetItem = null;
    }
}
