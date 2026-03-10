package net.hallowed.oldways.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;

import java.util.Objects;

public class AutoRefill {

    // ---- Persistent state (replaces per-tick record + copies) ----
    private static ItemStack lastMainHand = ItemStack.EMPTY;
    private static ItemStack lastOffHand  = ItemStack.EMPTY;
    private static int lastSelectedSlot   = -1;
    private static int tickDelay          = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null || client.gameMode == null
                    || player.isCreative() || player.isSpectator()) {
                resetState();
                return;
            }

            // Cooldown after a refill — just update snapshots and wait
            if (tickDelay > 0) {
                tickDelay--;
                snapshotIfChanged(player);
                return;
            }

            ItemStack currentMain = player.getMainHandItem();
            ItemStack currentOff  = player.getOffhandItem();
            int       currentSlot = player.getInventory().getSelectedSlot();

            if (client.screen == null && lastSelectedSlot != -1 && !player.isDeadOrDying()) {
                // Only attempt refill if slot didn't change and cursor is empty
                if (currentSlot == lastSelectedSlot && player.containerMenu.getCarried().isEmpty()) {
                    // Skip if the player just pressed 'F' to swap hands
                    if (!isHandSwap(lastMainHand, lastOffHand, currentMain, currentOff)) {
                        boolean refilledMain = checkAndRefill(
                                client, player, InteractionHand.MAIN_HAND, lastMainHand, currentMain);
                        if (!refilledMain) {
                            checkAndRefill(
                                    client, player, InteractionHand.OFF_HAND, lastOffHand, currentOff);
                        }
                    }
                }
            }

            // Only copy when something actually changed (zero copies in the common case)
            snapshotIfChanged(player);
        });
    }

    // ---- Snapshot helpers ----

    private static void resetState() {
        lastMainHand    = ItemStack.EMPTY;
        lastOffHand     = ItemStack.EMPTY;
        lastSelectedSlot = -1;
    }

    /**
     * Compares live stacks to stored snapshots; only copies when a difference is found.
     */
    private static void snapshotIfChanged(LocalPlayer player) {
        ItemStack liveMain = player.getMainHandItem();
        ItemStack liveOff  = player.getOffhandItem();

        if (!stacksMatch(lastMainHand, liveMain)) lastMainHand = liveMain.copy();
        if (!stacksMatch(lastOffHand, liveOff))   lastOffHand  = liveOff.copy();
        lastSelectedSlot = player.getInventory().getSelectedSlot();
    }

    /**
     * Lightweight comparison — no allocations.
     * Returns true when the stored snapshot still matches the live stack.
     */
    private static boolean stacksMatch(ItemStack stored, ItemStack live) {
        if (stored.isEmpty() && live.isEmpty()) return true;
        if (stored.isEmpty() || live.isEmpty()) return false;
        return stored.getCount() == live.getCount()
                && ItemStack.isSameItemSameComponents(stored, live);
    }

    // ---- Hand-swap detection ----

    private static boolean isHandSwap(ItemStack lastMain, ItemStack lastOff,
                                      ItemStack curMain,  ItemStack curOff) {
        return isExactMatch(lastMain, curOff) && isExactMatch(lastOff, curMain);
    }

    private static boolean isExactMatch(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b);
    }

    // ---- Refill logic (unchanged behavior) ----

    private static boolean checkAndRefill(Minecraft client, LocalPlayer player,
                                          InteractionHand hand,
                                          ItemStack last, ItemStack current) {
        if (last.isEmpty()) return false;
        if (!isNeedsRefill(last, current)) return false;

        Inventory inv = player.getInventory();
        int slotToRefillFrom = -1;

        for (int i = 35; i >= 9; i--) {
            ItemStack slotStack = inv.getItem(i);
            if (!slotStack.is(last.getItem())) continue;

            if (last.getItem() instanceof PotionItem) {
                if (!Objects.equals(
                        last.get(DataComponents.POTION_CONTENTS),
                        slotStack.get(DataComponents.POTION_CONTENTS))) {
                    continue;
                }
            }
            slotToRefillFrom = i;
            break;
        }

        if (slotToRefillFrom == -1) return false;

        int syncId = player.inventoryMenu.containerId;

        // Shift-click leftover items (empty buckets, near-broken rods) into inventory first
        if (!current.isEmpty()) {
            int handSlot = hand == InteractionHand.MAIN_HAND
                    ? 36 + inv.getSelectedSlot() : 45;
            client.gameMode.handleInventoryMouseClick(
                    syncId, handSlot, 0, ClickType.QUICK_MOVE, player);
        }

        int hotbarButton = hand == InteractionHand.MAIN_HAND
                ? inv.getSelectedSlot() : 40;
        client.gameMode.handleInventoryMouseClick(
                syncId, slotToRefillFrom, hotbarButton, ClickType.SWAP, player);

        tickDelay = 3;
        return true;
    }

    private static boolean isNeedsRefill(ItemStack last, ItemStack current) {
        // Depleted completely
        if (current.isEmpty()) return true;

        // Turned into a generic container (bucket, bowl, bottle)
        if (current.getCount() == 1 && !current.is(last.getItem())) {
            if (current.is(Items.GLASS_BOTTLE)
                    || current.is(Items.BUCKET)
                    || current.is(Items.BOWL)) {
                return true;
            }
        }

        // Fishing rod about to break
        if (last.getItem() instanceof FishingRodItem
                && current.getItem() instanceof FishingRodItem) {
            return current.getMaxDamage() - current.getDamageValue() < 5;
        }

        return false;
    }
}