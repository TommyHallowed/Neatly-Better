package net.hallowed.oldways.content.feature;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SplashPotionItem;

/**
 * Adds a 1-second (20 tick) cooldown to splash and lingering potions on use.

 * The cooldown is queued and applied at END_SERVER_TICK — not during
 * UseItemCallback — because the callback fires before vanilla's cooldown
 * check in useItem(). Adding it immediately would block the throw.
 */
public final class PotionCooldownHandler {

    private PotionCooldownHandler() {}

    /** Cooldowns to apply at the end of the current tick. */
    private static final List<Runnable> PENDING_COOLDOWNS = new ArrayList<>();

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (player.getAbilities().instabuild) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof SplashPotionItem
                    || stack.getItem() instanceof LingeringPotionItem) {
                if (!player.getCooldowns().isOnCooldown(stack)) {
                ItemStack stackCopy = stack.copy();
                PENDING_COOLDOWNS.add(() -> {
                    if (!player.isRemoved()) {
                        player.getCooldowns().addCooldown(stackCopy, 20);
                    }
                });
            }
            }

            return InteractionResult.PASS;
        });

        // Apply queued cooldowns after all item uses have been processed
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!PENDING_COOLDOWNS.isEmpty()) {
                PENDING_COOLDOWNS.forEach(Runnable::run);
                PENDING_COOLDOWNS.clear();
            }
        });
    }
}