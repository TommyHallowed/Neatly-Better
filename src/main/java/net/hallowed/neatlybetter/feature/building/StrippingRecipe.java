package net.hallowed.neatlybetter.feature.building;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class StrippingRecipe extends CustomRecipe {
    public static final RecipeSerializer<StrippingRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(StrippingRecipe::new),
            StreamCodec.unit(new StrippingRecipe()));

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath("neatly-better", "stripping"), SERIALIZER);
    }

    private record Match(int axeSlot, ItemStack axe, Item stripped, int count) {}

    private static @Nullable Item strippedOf(ItemStack s) {
        if (!(s.getItem() instanceof BlockItem bi)) return null;
        Block block = bi.getBlock();
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return BuiltInRegistries.BLOCK
                .getOptional(id.withPath("stripped_" + id.getPath()))
                .map(Block::asItem)
                .orElse(null);
    }

    private static @Nullable Match scan(CraftingInput input) {
        int axeSlot = -1, count = 0;
        Item source = null, stripped = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(ItemTags.AXES)) {
                if (axeSlot != -1) return null;
                axeSlot = i;
                continue;
            }
            Item out = strippedOf(s);
            if (out == null || (source != null && s.getItem() != source)) return null;
            source = s.getItem();
            stripped = out;
            count++;
        }
        return axeSlot == -1 || count == 0 ? null : new Match(axeSlot, input.getItem(axeSlot), stripped, count);
    }

    @Override
    public boolean matches(@NonNull CraftingInput input, @NonNull Level level) { return scan(input) != null; }

    @Override
    public @NonNull ItemStack assemble(@NonNull CraftingInput input) {
        Match m = scan(input);
        return m == null ? ItemStack.EMPTY : new ItemStack(m.stripped(), m.count());
    }

    @Override
    public @NonNull NonNullList<ItemStack> getRemainingItems(@NonNull CraftingInput input) {
        NonNullList<ItemStack> rem = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        Match m = scan(input);
        if (m == null) return rem;

        ItemStack axe = m.axe().copy();
        if (axe.isDamageableItem()) {
            int unbreaking = 0;
            for (var e : axe.getEnchantments().entrySet())
                if (e.getKey().is(Enchantments.UNBREAKING)) unbreaking = e.getIntValue();

            RandomSource rng = RandomSource.create();
            int damage = 0;
            for (int i = 0; i < m.count(); i++)
                if (rng.nextInt(unbreaking + 1) == 0) damage++;

            int newDamage = axe.getDamageValue() + damage;
            if (newDamage >= axe.getMaxDamage()) axe = ItemStack.EMPTY;
            else axe.setDamageValue(newDamage);
        }
        rem.set(m.axeSlot(), axe);
        return rem;
    }

    @Override
    public @NonNull RecipeSerializer<? extends CustomRecipe> getSerializer() { return SERIALIZER; }
}