package net.hallowed.oldways.content.feature;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.hallowed.oldways.api.events.AnvilUpdateEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

public final class MendingNerf {
    private MendingNerf() {}

    public static void init() {
        AnvilUpdateEvent.EVENT.register(event -> {

            ItemStack left  = event.getLeft();
            ItemStack right = event.getRight();
            if (left.isEmpty() || right.isEmpty() || !right.is(Items.ENCHANTED_BOOK))
                return InteractionResult.PASS;

            ItemEnchantments stored = right.getOrDefault(
                    DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (!containsEnchant(stored, Enchantments.MENDING)) return InteractionResult.PASS;
            int storedCount = 0;
            for (Entry<Holder<@NotNull Enchantment>> ignored : stored.entrySet()) storedCount++;
            if (storedCount != 1) return InteractionResult.PASS;

            boolean hasDurabilityTag = left.is(ItemTags.DURABILITY_ENCHANTABLE);
            boolean isDamageable     = left.isDamageableItem();
            if (!hasDurabilityTag || !isDamageable) {
                return InteractionResult.FAIL;
            }

            ItemEnchantments live = left.getOrDefault(
                    DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

            boolean hasConflict = containsEnchant(live, Enchantments.INFINITY);

            boolean hasRepairCost = left.get(DataComponents.REPAIR_COST) != null;

            if (hasConflict || hasRepairCost) {
                ItemStack out = left.copy();

                if (!live.isEmpty() && containsEnchant(live, Enchantments.MENDING)) {
                    ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(live);
                    b.removeIf(e -> e.is(Enchantments.MENDING));
                    out.set(DataComponents.ENCHANTMENTS, b.toImmutable());
                }

                out.remove(DataComponents.REPAIR_COST);

                event.setOutput(out);
                event.setCost(2);
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        });
    }

    public static boolean containsEnchant(ItemEnchantments comp, ResourceKey<@NotNull Enchantment> key) {
        if (comp.isEmpty()) return false;
        for (Entry<Holder<@NotNull Enchantment>> e : comp.entrySet()) {
            if (e.getKey().is(key)) return true;
        }
        return false;
    }
}
