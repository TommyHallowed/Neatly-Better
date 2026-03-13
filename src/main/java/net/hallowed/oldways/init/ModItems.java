package net.hallowed.oldways.init;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.item.MapBuilderItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModItems {
    private ModItems() {}

    public static Item MAP_BUILDER, CHEST_KEY;

    public static void register() {

        MAP_BUILDER = OWRegistry.registerItem("map_builder",
                new MapBuilderItem(new Item.Properties()
                        .stacksTo(1)
                        .setId(OWRegistry.itemKey("map_builder"))));

        CHEST_KEY = OWRegistry.registerItem("chest_key",
                new Item(new Item.Properties()
                        .stacksTo(1)
                        .setId(OWRegistry.itemKey("chest_key"))));

        // Server-side Left-Click block detection for Corner 2
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClientSide() && !player.isSpectator()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof MapBuilderItem builder) {
                    builder.onLeftClickBlock(pos, stack);
                    return InteractionResult.SUCCESS; // Cancel block breaking
                }
            }
            return InteractionResult.PASS;
        });
    }
}