package net.hallowed.neatlybetter.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.component.UseRemainder;

import java.util.Objects;
import java.util.Set;

public class AutoRefill {

    private static final Set<Identifier> BLACKLISTED_ITEMS = Set.of(
            Identifier.parse("neatly-better:chest_key")
    );

    private static ItemStack lastMainHand = ItemStack.EMPTY;
    private static ItemStack lastOffHand  = ItemStack.EMPTY;
    private static int lastSelectedSlot   = -1;
    private static int tickDelay          = 0;

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

    private static boolean isBlacklisted(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return BLACKLISTED_ITEMS.contains(id);
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

    private static boolean isNeedsRefill(ItemStack last, ItemStack current) {
        if (current.isEmpty()) return true;

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

        if (last.getItem() instanceof FishingRodItem
                && current.getItem() instanceof FishingRodItem) {
            return current.getMaxDamage() - current.getDamageValue() < 5;
        }

        return false;
    }
}
