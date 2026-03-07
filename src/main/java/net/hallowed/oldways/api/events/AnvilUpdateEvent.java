package net.hallowed.oldways.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class AnvilUpdateEvent {
    public interface AnvilUpdateCallback {
        InteractionResult update(AnvilUpdateEvent event);
    }

    public static final Event<@NotNull AnvilUpdateCallback> EVENT =
            EventFactory.createArrayBacked(AnvilUpdateCallback.class, listeners -> event -> {
                for (AnvilUpdateCallback l : listeners) {
                    InteractionResult r = l.update(event);
                    if (r != InteractionResult.PASS) return r;
                }
                return InteractionResult.PASS;
            });

    private final ItemStack left, right;
    private ItemStack output;
    private int cost;

    public AnvilUpdateEvent(ItemStack left, ItemStack right, int cost) {
        this.left = left;
        this.right = right;
        this.output = ItemStack.EMPTY;
        this.cost = cost;
    }

    public ItemStack getLeft() { return left; }
    public ItemStack getRight() { return right; }
    public ItemStack getOutput() { return output; }
    public void setOutput(ItemStack output) { this.output = output; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
}
