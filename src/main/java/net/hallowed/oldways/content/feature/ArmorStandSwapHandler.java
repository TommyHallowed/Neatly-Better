package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

/**
 * Shift + right-click an armor stand to swap ALL four armor slots at once.
 * <p>
 * Registered via Fabric's {@link UseEntityCallback} — no mixins required.
 * <p>
 * Sound: {@code setItemSlot()} internally calls {@code onEquipItem()}, which
 * plays each piece's own equip sound automatically — no manual sound needed.
 * <p>
 * Hand swing: client swings from the {@code SUCCESS} return; server broadcasts
 * the swing to other players via {@code player.swing(hand, true)}.
 * <p>
 * Slot locks: respects {@code canUseSlot()} — locked slots on map-maker
 * armor stands are skipped.
 */
public class ArmorStandSwapHandler {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // ── Gate: main hand + sneaking + armor stand ────────────────
            if (hand != InteractionHand.MAIN_HAND
                    || !player.isShiftKeyDown()
                    || !(entity instanceof ArmorStand armorStand)) {
                return InteractionResult.PASS;
            }

            // Skip marker stands (invisible hitbox, used by map-makers) and spectators
            if (armorStand.isMarker() || player.isSpectator()) {
                return InteractionResult.PASS;
            }

            // ── Client side ────────────────────────────────────────────
            // Return SUCCESS to:
            //   1. Swing the hand locally (shouldSwing() == true)
            //   2. Send the interaction packet to the server
            //   3. Consume the event so vanilla's single-piece handler doesn't run
            if (world.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            // ── Server side ────────────────────────────────────────────
            boolean swapped = false;

            for (EquipmentSlot slot : ARMOR_SLOTS) {
                // Respect armor stand slot locks (DisabledSlots NBT)
                if (!armorStand.canUseSlot(slot)) continue;

                ItemStack standItem = armorStand.getItemBySlot(slot);
                ItemStack playerItem = player.getItemBySlot(slot);

                // Nothing to swap in this slot
                if (standItem.isEmpty() && playerItem.isEmpty()) continue;

                // Swap — copy both before setting to avoid aliasing
                ItemStack toPlayer = standItem.copy();
                ItemStack toStand = playerItem.copy();

                // setItemSlot → onEquipItem → plays each piece's equip sound automatically
                armorStand.setItemSlot(slot, toStand);
                player.setItemSlot(slot, toPlayer);

                swapped = true;
            }

            if (swapped) {
                // Broadcast hand swing to other players (local player already swung on client)
                player.swing(InteractionHand.MAIN_HAND, true);
                return InteractionResult.SUCCESS;
            }

            // Nothing was swapped (all slots empty or locked) — consume anyway
            // to prevent vanilla's single-piece handler from running while sneaking
            return InteractionResult.SUCCESS;
        });
    }
}
