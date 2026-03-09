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

    private static PlayerHandState lastState = null;
    private static int tickDelay = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null || client.gameMode == null || player.isCreative() || player.isSpectator()) {
                lastState = null;
                return;
            }

            // A small delay after refilling prevents the client from sending duplicate
            // packets to the server before the inventory has fully stabilized.
            if (tickDelay > 0) {
                tickDelay--;
                lastState = new PlayerHandState(player.getInventory().getSelectedSlot(), player.getMainHandItem().copy(), player.getOffhandItem().copy());
                return;
            }

            PlayerHandState currentState = new PlayerHandState(
                    player.getInventory().getSelectedSlot(),
                    player.getMainHandItem().copy(),
                    player.getOffhandItem().copy()
            );

            if (client.screen == null && lastState != null && !player.isDeadOrDying()) {
                // Ensure the player didn't manually change slots and isn't holding an item with their cursor
                if (currentState.selectedSlot() == lastState.selectedSlot() && player.containerMenu.getCarried().isEmpty()) {

                    boolean refilledMain = checkAndRefill(client, player, InteractionHand.MAIN_HAND, lastState.mainHand(), currentState.mainHand());
                    if (!refilledMain) {
                        checkAndRefill(client, player, InteractionHand.OFF_HAND, lastState.offHand(), currentState.offHand());
                    }
                }
            }

            lastState = new PlayerHandState(player.getInventory().getSelectedSlot(), player.getMainHandItem().copy(), player.getOffhandItem().copy());
        });
    }

    private static boolean checkAndRefill(Minecraft client, LocalPlayer player, InteractionHand hand, ItemStack last, ItemStack current) {
        if (last.isEmpty()) return false;

        boolean needsRefill = isNeedsRefill(last, current);

        if (needsRefill) {
            Inventory inv = player.getInventory();
            int slotToRefillFrom = -1;

            // Scan the main inventory (slots 9 to 35) backwards
            for (int i = 35; i >= 9; i--) {
                ItemStack slotStack = inv.getItem(i);
                if (slotStack.is(last.getItem())) {
                    if (last.getItem() instanceof PotionItem) {
                        if (!Objects.equals(last.get(DataComponents.POTION_CONTENTS), slotStack.get(DataComponents.POTION_CONTENTS))) {
                            continue;
                        }
                    }
                    slotToRefillFrom = i;
                    break;
                }
            }

            if (slotToRefillFrom != -1) {
                int syncId = player.inventoryMenu.containerId;

                // If there is a leftover item (like an empty bucket or nearly broken rod),
                // we simulate a Shift-Click to throw it into the inventory first.
                if (!current.isEmpty()) {
                    int handSlot = hand == InteractionHand.MAIN_HAND ? 36 + inv.getSelectedSlot() : 45;
                    client.gameMode.handleInventoryMouseClick(syncId, handSlot, 0, ClickType.QUICK_MOVE, player);
                }

                // Simulate hovering over the replacement item and pressing the hotbar key to swap it into the hand
                int hotbarButton = hand == InteractionHand.MAIN_HAND ? inv.getSelectedSlot() : 40; // 40 is the hardcoded button ID for swapping to offhand
                client.gameMode.handleInventoryMouseClick(syncId, slotToRefillFrom, hotbarButton, ClickType.SWAP, player);

                tickDelay = 3;
                return true;
            }
        }
        return false;
    }

    private static boolean isNeedsRefill(ItemStack last, ItemStack current) {
        boolean needsRefill = false;

        // 1. Depleted completely
        if (current.isEmpty()) {
            needsRefill = true;
        }
        // 2. Turned into a generic container (buckets, bowls, bottles)
        else if (current.getCount() == 1 && !current.is(last.getItem())) {
            if (current.is(Items.GLASS_BOTTLE) || current.is(Items.BUCKET) || current.is(Items.BOWL)) {
                needsRefill = true;
            }
        }
        // 3. Fishing rod about to break
        else if (last.getItem() instanceof FishingRodItem && current.getItem() instanceof FishingRodItem) {
            int damage = current.getDamageValue();
            int maxDamage = current.getMaxDamage();
            if (maxDamage - damage < 5) {
                needsRefill = true;
            }
        }
        return needsRefill;
    }

    private record PlayerHandState(int selectedSlot, ItemStack mainHand, ItemStack offHand) {}
}