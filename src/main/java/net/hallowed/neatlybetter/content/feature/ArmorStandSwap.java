package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

public class ArmorStandSwap {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, _) -> {

            if (!NTServerConfig.CONFIG.armorStandSwap.get()) return InteractionResult.PASS;

            if (hand != InteractionHand.MAIN_HAND
                        || !player.isShiftKeyDown()
                        || !(entity instanceof ArmorStand armorStand)) {
                    return InteractionResult.PASS;
                }
                if (armorStand.isMarker() || player.isSpectator()) {return InteractionResult.PASS;}

                if (world.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }

                boolean swapped = false;

                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    if (!armorStand.canUseSlot(slot)) continue;

                    ItemStack standItem = armorStand.getItemBySlot(slot);
                    ItemStack playerItem = player.getItemBySlot(slot);

                    if (standItem.isEmpty() && playerItem.isEmpty()) continue;

                    ItemStack toPlayer = standItem.copy();
                    ItemStack toStand = playerItem.copy();

                    armorStand.setItemSlot(slot, toStand);
                    player.setItemSlot(slot, toPlayer);

                    swapped = true;
                }

                if (swapped) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.SUCCESS;
            });
    }
}
