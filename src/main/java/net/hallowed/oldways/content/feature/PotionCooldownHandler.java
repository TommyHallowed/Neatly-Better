package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SplashPotionItem;

/**
 * Adds a 1-second (20 tick) cooldown to splash and lingering potions on use.
 * Replaces LingeringPotionItemMixin and SplashPotionItemMixin.

 * Place in: content/feature/
 */
public final class PotionCooldownHandler {

    private PotionCooldownHandler() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (player.getAbilities().instabuild) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof SplashPotionItem
                    || stack.getItem() instanceof LingeringPotionItem) {
                player.getCooldowns().addCooldown(stack, 20);
            }

            // Always PASS — we never cancel the use, just add the cooldown before vanilla processes it
            return InteractionResult.PASS;
        });
    }
}
