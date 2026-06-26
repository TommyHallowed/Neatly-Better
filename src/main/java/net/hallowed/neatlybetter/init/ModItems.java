package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.hallowed.neatlybetter.content.component.QuiverContents;
import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.content.item.QuiverItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static net.hallowed.neatlybetter.init.ModBlocks.RAINBOW_BED;

public final class ModItems {
    private ModItems() {}

    public static Item MAP_BUILDER, CHEST_KEY, WOLF_COLLAR, QUIVER;

    public static void register() {

        MAP_BUILDER = NTRegistry.registerItem("map_builder",
                new MapBuilderItem(new Item.Properties()
                        .stacksTo(1)
                        .setId(NTRegistry.itemKey("map_builder"))));

        CHEST_KEY = NTRegistry.registerItem("chest_key",
                new Item(new Item.Properties()
                        .stacksTo(1)
                        .setId(NTRegistry.itemKey("chest_key"))));

        NTRegistry.registerItem("rainbow_bed",
                new BedItem(RAINBOW_BED, new Item.Properties()
                        .stacksTo(1)
                        .setId(NTRegistry.itemKey("rainbow_bed"))));

        WOLF_COLLAR = NTRegistry.registerItem("wolf_collar",
                new Item(new Item.Properties()
                        .stacksTo(1)
                        .fireResistant()
                        .setId(NTRegistry.itemKey("wolf_collar"))));

        QUIVER = NTRegistry.registerItem("quiver",
                new QuiverItem(new Item.Properties()
                        .stacksTo(1)
                        .setId(NTRegistry.itemKey("quiver"))
                        .component(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY)));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, _) -> {
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