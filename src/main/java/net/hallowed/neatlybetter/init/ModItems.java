package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModItems {
    private ModItems() {}

    public static Item MAP_BUILDER, CHEST_KEY;

    public static void register() {

        MAP_BUILDER = NTRegistry.registerItem("map_builder",
                new MapBuilderItem(new Item.Properties()
                        .stacksTo(1)
                        .setId(NTRegistry.itemKey("map_builder"))));

        CHEST_KEY = NTRegistry.registerItem("chest_key",
                new Item(new Item.Properties()
                        .stacksTo(1)
                        .setId(NTRegistry.itemKey("chest_key"))));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClientSide() && !player.isSpectator()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof MapBuilderItem builder) {
                    builder.onLeftClickBlock(pos, stack);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }
}