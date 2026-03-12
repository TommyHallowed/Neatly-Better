package net.hallowed.oldways.content.entity.ai.goal;

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

/**
 * AI Goal that makes breedable animals walk toward and consume food
 * {@link ItemEntity}s on the ground, entering love mode just like
 * hand-feeding. Vanilla breeding (hand-feeding, cooldowns, mate-finding)
 * is completely untouched.
 *
 * <h3>Safety guarantees</h3>
 * <ul>
 *   <li>{@code getAge() == 0} — won't eat while baby OR on breeding cooldown</li>
 *   <li>{@code canFallInLove()} — won't eat if already in love</li>
 *   <li>{@code isPanicking()} — won't eat while fleeing</li>
 *   <li>Respects {@link ItemEntity#hasPickUpDelay()} — small grace period after throw</li>
 *   <li>Consumes exactly 1 item per love-mode entry</li>
 *   <li>Credits the item thrower as love-cause for breeding XP/stats</li>
 * </ul>
 *
 * <h3>Performance</h3>
 * <ul>
 *   <li>{@code canUse()} is throttled to run every ~20 game ticks (10 goal-selector cycles)</li>
 *   <li>AABB entity scan is limited to a 5-block radius</li>
 *   <li>Does NOT require update every tick — runs on the standard 2-tick goal cycle</li>
 * </ul>
 */
public class GroundItemBreedGoal extends Goal {

    /** How far an animal will detect food items on the ground (blocks). */
    private static final double SEARCH_RANGE = 5.0;

    /** Squared distance at which the animal "eats" the item (~1.5 blocks). */
    private static final double EAT_RANGE_SQ = 2.25;

    /**
     * How many goal-selector evaluation cycles to skip between entity scans.
     * The goal selector evaluates {@code canUse()} every 2 game ticks, so
     * a cooldown of 10 means ~20 game ticks (~1 second) between scans.
     */
    private static final int SEARCH_COOLDOWN = 10;

    private final Animal animal;
    private @Nullable ItemEntity targetItem;
    private int searchCooldown;

    public GroundItemBreedGoal(Animal animal) {
        this.animal = animal;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ── canUse ──────────────────────────────────────────────────────────

    @Override
    public boolean canUse() {
        // Throttle expensive entity scan
        if (--searchCooldown > 0) return false;
        searchCooldown = SEARCH_COOLDOWN;

        // Mirrors vanilla's mobInteract guard:  age == 0 && canFallInLove()
        if (animal.getAge() != 0) return false;   // baby (< 0) or cooldown (> 0)
        if (!animal.canFallInLove()) return false; // already in love
        if (animal.isPanicking()) return false;

        // Search nearby ground items
        AABB box = animal.getBoundingBox().inflate(SEARCH_RANGE);
        List<ItemEntity> items = animal.level().getEntitiesOfClass(
                ItemEntity.class, box,
                ie -> !ie.isRemoved()
                        && !ie.hasPickUpDelay()
                        && animal.isFood(ie.getItem())
        );
        if (items.isEmpty()) return false;

        // Pick closest
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

    // ── canContinueToUse ────────────────────────────────────────────────

    @Override
    public boolean canContinueToUse() {
        if (targetItem == null || targetItem.isRemoved()) return false;
        if (!animal.isFood(targetItem.getItem())) return false;
        if (animal.getAge() != 0) return false;
        if (!animal.canFallInLove()) return false;
        return !animal.isPanicking();
    }

    // ── start / tick / stop ─────────────────────────────────────────────

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

        if (animal.distanceToSqr(targetItem) < EAT_RANGE_SQ) {
            consumeOneItem();
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        animal.getNavigation().stop();
    }

    // ── item consumption ────────────────────────────────────────────────

    private void consumeOneItem() {
        if (targetItem == null || targetItem.isRemoved()) return;

        ItemStack stack = targetItem.getItem();
        if (stack.isEmpty()) return;

        // Last-second re-check (another animal may have eaten it this tick)
        if (animal.getAge() != 0 || !animal.canFallInLove()) return;

        // Credit the player who threw the item (for breeding XP / stats)
        Player thrower = null;
        Entity owner = targetItem.getOwner();
        if (owner instanceof Player p) {
            thrower = p;
        }

        // Consume exactly one item from the stack
        if (stack.getCount() <= 1) {
            targetItem.discard();
        } else {
            targetItem.setItem(stack.copyWithCount(stack.getCount() - 1));
        }

        // Enter love mode — vanilla BreedGoal / AnimalMakeLove takes over from here
        animal.setInLove(thrower);
        targetItem = null;
    }
}
