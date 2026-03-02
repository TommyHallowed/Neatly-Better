package net.hallowed.oldways.init;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.item.MapBuilderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public final class ModItems {
    private ModItems() {}

    public static Item DRAGON_BURST_ROCKET, MAP_BUILDER;

    public static void register() {

        MAP_BUILDER = OWRegistry.registerItem("map_builder",
                new MapBuilderItem(new Item.Settings()
                        .maxCount(1)
                        .registryKey(OWRegistry.itemKey("map_builder"))));

        // Server-side Left-Click block detection for Corner 2
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClient() && !player.isSpectator()) {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem() instanceof MapBuilderItem builder) {
                    builder.onLeftClickBlock(player, pos, stack);
                    return ActionResult.SUCCESS; // Cancel block breaking
                }
            }
            return ActionResult.PASS;
        });
    }
}