package net.hallowed.neatlybetter.content.feature;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SplashPotionItem;

public final class ItemCooldownHandler {

    private ItemCooldownHandler() {}

    private static final List<Runnable> PENDING_COOLDOWNS = new ArrayList<>();

    private static ItemStack totemStack;

    public static ItemStack getTotemStack() {
        if (totemStack == null) {
            totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);
        }
        return totemStack;
    }

    public static boolean isTotemOnCooldown(Player player) {
        return player.getCooldowns().isOnCooldown(getTotemStack());
    }

    public static void applyTotemCooldown(Player player) {

        int seconds = NTServerConfig.CONFIG.totemCooldown.get();
        if (seconds != 0) {
            player.getCooldowns().addCooldown(getTotemStack(), seconds * 20);
        }
    }

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

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!PENDING_COOLDOWNS.isEmpty()) {
                PENDING_COOLDOWNS.forEach(Runnable::run);
                PENDING_COOLDOWNS.clear();
            }
        });
    }
}