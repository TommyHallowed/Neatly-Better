package net.hallowed.neatlybetter.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ColorCollection;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CauldronDyeData {

    private CauldronDyeData() {
    }

    public record BlockDyeFamily(ColorCollection<Item> colored, @Nullable Item plain, int batchSize) {
    }

    public static final List<BlockDyeFamily> BLOCK_DYE_FAMILIES = List.of(
            new BlockDyeFamily(Items.WOOL, Items.WOOL.white(), 8),
            new BlockDyeFamily(Items.CARPET, Items.CARPET.white(), 16),
            new BlockDyeFamily(Items.DYED_TERRACOTTA, Items.TERRACOTTA, 8),
            new BlockDyeFamily(Items.CONCRETE, null, 8),
            new BlockDyeFamily(Items.STAINED_GLASS, Items.GLASS, 8),
            new BlockDyeFamily(Items.STAINED_GLASS_PANE, Items.GLASS_PANE, 8),
            new BlockDyeFamily(Items.DYED_SHULKER_BOX, Items.SHULKER_BOX, 1),
            new BlockDyeFamily(Items.BED, Items.BED.white(), 1),
            new BlockDyeFamily(Items.DYED_CANDLE, Items.CANDLE, 8)
    );

    public static final Set<Item> DYEABLE_ITEMS = Set.of(
            Items.LEATHER_HELMET,
            Items.LEATHER_CHESTPLATE,
            Items.LEATHER_LEGGINGS,
            Items.LEATHER_BOOTS,
            Items.LEATHER_HORSE_ARMOR,
            Items.WOLF_ARMOR
    );

    public static final Map<Item, BlockDyeFamily> BLOCK_DYE_FAMILY_BY_ITEM = new HashMap<>();
    public static final Map<Item, DyeColor> BLOCK_ITEM_COLORS = new HashMap<>();
    public static final Map<Integer, DyeColor> RGB_TO_DYE_COLOR = new HashMap<>();

    static {
        for (BlockDyeFamily family : BLOCK_DYE_FAMILIES) {
            if (family.plain() != null) {
                BLOCK_DYE_FAMILY_BY_ITEM.put(family.plain(), family);
            }

            List<Item> coloredItems = family.colored().asList();
            for (int i = 0; i < DyeColor.VALUES.size(); i++) {
                Item item = coloredItems.get(i);
                DyeColor color = DyeColor.VALUES.get(i);
                BLOCK_DYE_FAMILY_BY_ITEM.put(item, family);
                BLOCK_ITEM_COLORS.put(item, color);
            }
        }

        for (DyeColor color : DyeColor.VALUES) {
            RGB_TO_DYE_COLOR.put(color.getTextureDiffuseColor(), color);
        }
    }

    public static boolean isDyeable(ItemStack stack) {
        return DYEABLE_ITEMS.contains(stack.getItem());
    }
}