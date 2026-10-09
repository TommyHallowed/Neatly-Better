package net.hallowed.neatlybetter.client.content.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.component.UseRemainder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AutoRefill {

    private static ItemStack lastMainHand = ItemStack.EMPTY;
    private static ItemStack lastOffHand  = ItemStack.EMPTY;
    private static int lastSelectedSlot   = -1;
    private static int tickDelay          = 0;

    private static List<? extends String> cachedBlacklistSource = null;
    private static List<Pattern> cachedBlacklist = List.of();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!NTClientConfig.CONFIG.autoRefill.get()) {
                resetState();
                return;
            }

            LocalPlayer player = client.player;
            if (player == null || client.gameMode == null
                    || player.isCreative() || player.isSpectator()) {
                resetState();
                return;
            }

            if (tickDelay > 0) {
                tickDelay--;
                snapshotIfChanged(player);
                return;
            }

            ItemStack currentMain = player.getMainHandItem();
            ItemStack currentOff  = player.getOffhandItem();
            int       currentSlot = player.getInventory().getSelectedSlot();

            if (client.gui.screen() == null && lastSelectedSlot != -1 && !player.isDeadOrDying()) {
                if (currentSlot == lastSelectedSlot && player.containerMenu.getCarried().isEmpty()) {
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
            snapshotIfChanged(player);
        });
    }


    private static void resetState() {
        lastMainHand    = ItemStack.EMPTY;
        lastOffHand     = ItemStack.EMPTY;
        lastSelectedSlot = -1;
    }

    private static void snapshotIfChanged(LocalPlayer player) {
        ItemStack liveMain = player.getMainHandItem();
        ItemStack liveOff  = player.getOffhandItem();

        if (stacksDontMatch(lastMainHand, liveMain)) lastMainHand = liveMain.copy();
        if (stacksDontMatch(lastOffHand, liveOff))   lastOffHand  = liveOff.copy();
        lastSelectedSlot = player.getInventory().getSelectedSlot();
    }

    private static boolean stacksDontMatch(ItemStack stored, ItemStack live) {
        if (stored.isEmpty() && live.isEmpty()) return false;
        if (stored.isEmpty() || live.isEmpty()) return true;
        return stored.getCount() != live.getCount()
                || !ItemStack.isSameItemSameComponents(stored, live);
    }

    private static boolean isHandSwap(ItemStack lastMain, ItemStack lastOff,
                                      ItemStack curMain,  ItemStack curOff) {
        return isExactMatch(lastMain, curOff) && isExactMatch(lastOff, curMain);
    }

    private static boolean isExactMatch(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b);
    }

    /** Matches the item id against the configured regular expressions (compiled once per config change). */
    private static boolean isBlacklisted(ItemStack stack) {
        if (stack.isEmpty()) return false;

        List<? extends String> source = NTClientConfig.CONFIG.autoRefillBlacklist.get();
        if (source != cachedBlacklistSource) {
            List<Pattern> compiled = new ArrayList<>();
            for (String entry : source) {
                try {
                    compiled.add(Pattern.compile(entry, Pattern.CASE_INSENSITIVE));
                } catch (PatternSyntaxException ignored) {
                    // the config validator already rejects these; skip defensively
                }
            }
            cachedBlacklist = compiled;
            cachedBlacklistSource = source;
        }

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (Pattern pattern : cachedBlacklist) {
            if (pattern.matcher(id).matches()) return true;
        }
        return false;
    }

    private static boolean isLowDurability(ItemStack stack) {
        return stack.isDamageableItem()
                && stack.getMaxDamage() - stack.getDamageValue() <= 5;
    }

    /** Whether {@code candidate} may be used to refill {@code last}. Low-durability tools never qualify. */
    private static boolean isValidSource(ItemStack last, ItemStack candidate) {
        if (!candidate.is(last.getItem())) return false;

        if (last.getItem() instanceof PotionItem
                && !Objects.equals(
                last.get(DataComponents.POTION_CONTENTS),
                candidate.get(DataComponents.POTION_CONTENTS))) {
            return false;
        }

        return !isLowDurability(candidate);
    }

    private static boolean checkAndRefill(Minecraft client, LocalPlayer player,
                                          InteractionHand hand,
                                          ItemStack last, ItemStack current) {
        if (last.isEmpty()) return false;
        if (isBlacklisted(last)) return false;
        if (!isNeedsRefill(last, current)) return false;

        Inventory inv = player.getInventory();
        int slotToRefillFrom = -1;

        for (int i = 35; i >= 9; i--) {
            if (isValidSource(last, inv.getItem(i))) {
                slotToRefillFrom = i;
                break;
            }
        }

        if (slotToRefillFrom == -1) {
            // Nothing in the main inventory: switch to a matching hotbar slot instead (main hand only).
            return hand == InteractionHand.MAIN_HAND && selectFromHotbar(inv, last);
        }

        int syncId = player.inventoryMenu.containerId;

        if (!current.isEmpty()) {
            int handSlot = hand == InteractionHand.MAIN_HAND
                    ? 36 + inv.getSelectedSlot() : 45;
            assert client.gameMode != null;
            client.gameMode.handleContainerInput(
                    syncId, handSlot, 0, ContainerInput.QUICK_MOVE, player);
        }

        int hotbarButton = hand == InteractionHand.MAIN_HAND
                ? inv.getSelectedSlot() : 40;
        assert client.gameMode != null;
        client.gameMode.handleContainerInput(
                syncId, slotToRefillFrom, hotbarButton, ContainerInput.SWAP, player);

        tickDelay = 3;
        return true;
    }

    /** Selects the nearest other hotbar slot (scanning to the right, wrapping) holding a valid replacement. */
    private static boolean selectFromHotbar(Inventory inv, ItemStack last) {
        int selected = inv.getSelectedSlot();
        for (int offset = 1; offset < 9; offset++) {
            int slot = (selected + offset) % 9;
            if (isValidSource(last, inv.getItem(slot))) {
                inv.setSelectedSlot(slot);
                tickDelay = 3;
                return true;
            }
        }
        return false;
    }

    private static boolean isNeedsRefill(ItemStack last, ItemStack current) {
        if (current.isEmpty()) return true;

        // Swap out a tool that is about to break, as long as a healthy replacement exists.
        if (current.is(last.getItem()) && isLowDurability(current)) return true;

        if (current.getCount() == 1 && !current.is(last.getItem())) {

            UseRemainder remainder = last.get(DataComponents.USE_REMAINDER);
            if (remainder != null && remainder.convertInto().is(current.getItem())) {
                return true;
            }

            if (current.is(Items.GLASS_BOTTLE)
                    || current.getItem() instanceof BucketItem
                    || current.is(Items.BOWL)) {
                return true;
            }
        }

        return false;
    }
}