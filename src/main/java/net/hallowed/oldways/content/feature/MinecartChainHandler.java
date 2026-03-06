package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.hallowed.oldways.util.CartUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

import java.util.UUID;

public class MinecartChainHandler {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (player.isSneaking() && stack.isOf(Items.IRON_CHAIN) && entity instanceof AbstractMinecartEntity cart) {

                if (world.isClient()) return ActionResult.SUCCESS;

                NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

                if (!nbt.contains("LinkedCartId")) {
                    nbt.putString("LinkedCartId", cart.getUuid().toString());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                    world.playSound(null, cart.getBlockPos(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);

                } else {
                    String uuidString = nbt.getString("LinkedCartId").orElse("");
                    if (uuidString.startsWith("Optional[")) uuidString = uuidString.substring(9, uuidString.length() - 1);

                    UUID targetUuid;
                    try {
                        targetUuid = UUID.fromString(uuidString);
                    } catch (Exception e) {
                        nbt.remove("LinkedCartId");
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                        return ActionResult.SUCCESS;
                    }

                    nbt.remove("LinkedCartId");
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    if (!targetUuid.equals(cart.getUuid()) && world instanceof ServerWorld serverWorld) {
                        Entity e = serverWorld.getEntity(targetUuid);
                        if (e instanceof AbstractMinecartEntity targetCart) {

                            // targetCart = Engine, cart = Follower
                            CartUtils.linkTo(cart, targetCart, stack);

                            stack.decrementUnlessCreative(1, player);
                            world.playSound(null, cart.getBlockPos(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}