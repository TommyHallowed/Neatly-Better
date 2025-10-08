package net.hallowed.oldways.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.hallowed.oldways.api.events.AnvilUpdateEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;

public final class MendingNerf {
    private MendingNerf() {}

    public static void init() {
        AnvilUpdateEvent.EVENT.register(event -> {

            ItemStack left  = event.getLeft();
            ItemStack right = event.getRight();
            if (left.isEmpty() || right.isEmpty() || !right.isOf(Items.ENCHANTED_BOOK))
                return ActionResult.PASS;

            ItemEnchantmentsComponent stored = right.getOrDefault(
                    DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            if (!containsEnchant(stored, Enchantments.MENDING)) return ActionResult.PASS;
            int storedCount = 0;
            for (Entry<RegistryEntry<Enchantment>> ignored : stored.getEnchantmentEntries()) storedCount++;
            if (storedCount != 1) return ActionResult.PASS;

            boolean hasDurabilityTag = left.isIn(ItemTags.DURABILITY_ENCHANTABLE);
            boolean isDamageable     = left.isDamageable();
            if (!hasDurabilityTag || !isDamageable) {
                return ActionResult.FAIL;
            }

            ItemEnchantmentsComponent live = left.getOrDefault(
                    DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

            boolean hasConflict = containsEnchant(live, Enchantments.INFINITY);

            boolean hasRepairCost = left.get(DataComponentTypes.REPAIR_COST) != null;

            if (hasConflict || hasRepairCost) {
                ItemStack out = left.copy();

                if (!live.isEmpty() && containsEnchant(live, Enchantments.MENDING)) {
                    ItemEnchantmentsComponent.Builder b = new ItemEnchantmentsComponent.Builder(live);
                    b.remove(e -> e.matchesKey(Enchantments.MENDING));
                    out.set(DataComponentTypes.ENCHANTMENTS, b.build());
                }

                out.remove(DataComponentTypes.REPAIR_COST);

                event.setOutput(out);
                event.setCost(2);
                return ActionResult.CONSUME;
            }

            return ActionResult.PASS;
        });
    }

    public static boolean containsEnchant(ItemEnchantmentsComponent comp, RegistryKey<Enchantment> key) {
        if (comp.isEmpty()) return false;
        for (Entry<RegistryEntry<Enchantment>> e : comp.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(key)) return true;
        }
        return false;
    }
}
