package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.hallowed.oldways.util.CartUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import java.util.UUID;

public class MinecartChainHandler {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (player.isShiftKeyDown() && stack.is(Items.IRON_CHAIN) && entity instanceof AbstractMinecart cart) {

                if (world.isClientSide()) return InteractionResult.SUCCESS;

                CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

                if (!nbt.contains("LinkedCartId")) {
                    nbt.putString("LinkedCartId", cart.getUUID().toString());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                    world.playSound(null, cart.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

                } else {
                    String uuidString = nbt.getString("LinkedCartId").orElse("");
                    if (uuidString.startsWith("Optional[")) uuidString = uuidString.substring(9, uuidString.length() - 1);

                    UUID targetUuid;
                    try {
                        targetUuid = UUID.fromString(uuidString);
                    } catch (Exception e) {
                        nbt.remove("LinkedCartId");
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                        return InteractionResult.SUCCESS;
                    }

                    nbt.remove("LinkedCartId");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

                    if (!targetUuid.equals(cart.getUUID()) && world instanceof ServerLevel serverWorld) {
                        Entity e = serverWorld.getEntity(targetUuid);
                        if (e instanceof AbstractMinecart targetCart) {

                            // targetCart = Engine, cart = Follower
                            CartUtils.linkTo(cart, targetCart, stack);

                            stack.consume(1, player);
                            world.playSound(null, cart.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}