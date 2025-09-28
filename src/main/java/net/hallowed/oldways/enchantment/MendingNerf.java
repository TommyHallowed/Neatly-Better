package net.hallowed.oldways.enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.hallowed.oldways.api.events.AnvilUpdateEvent;
import net.hallowed.oldways.config.CommonConfigManager;
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
            if (!CommonConfigManager.mendingNerfEnabled()) return ActionResult.PASS;

            ItemStack left  = event.getLeft();
            ItemStack right = event.getRight();
            if (left.isEmpty() || right.isEmpty() || !right.isOf(Items.ENCHANTED_BOOK))
                return ActionResult.PASS;

            // --- Pure Mending book? ---
            ItemEnchantmentsComponent stored = right.getOrDefault(
                    DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            if (!containsEnchant(stored, Enchantments.MENDING)) return ActionResult.PASS;
            int storedCount = 0;
            for (Entry<RegistryEntry<Enchantment>> ignored : stored.getEnchantmentEntries()) storedCount++;
            if (storedCount != 1) return ActionResult.PASS;

            // --- Target eligibility (match vanilla Mending targets) ---
            boolean hasDurabilityTag = left.isIn(ItemTags.DURABILITY_ENCHANTABLE);
            boolean isDamageable     = left.isDamageable();
            if (!hasDurabilityTag || !isDamageable) {
                // Block the recipe entirely on ineligible items (prevents "enchanting" sticks, etc.)
                return ActionResult.FAIL;
            }

            // Live enchants on the target
            ItemEnchantmentsComponent live = left.getOrDefault(
                    DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

            // Conflict present? (vanilla: Mending ↔ Infinity)
            boolean hasConflict = containsEnchant(live, Enchantments.INFINITY);

            // Current repair cost state
            boolean hasRepairCost = left.get(DataComponentTypes.REPAIR_COST) != null; // component present => non-zero

            // If there is a conflict OR a non-zero repair cost -> perform RESET (consume book), never add Mending
            if (hasConflict || hasRepairCost) {
                ItemStack out = left.copy();

                // Strip existing Mending if somehow present already
                if (!live.isEmpty() && containsEnchant(live, Enchantments.MENDING)) {
                    ItemEnchantmentsComponent.Builder b = new ItemEnchantmentsComponent.Builder(live);
                    b.remove(e -> e.matchesKey(Enchantments.MENDING));
                    out.set(DataComponentTypes.ENCHANTMENTS, b.build());
                }

                // Reset repair cost to 0 (remove component)
                out.remove(DataComponentTypes.REPAIR_COST);

                event.setOutput(out);
                event.setCost(2);
                return ActionResult.CONSUME; // book consumed by your mixin's onTake
            }

            // Eligible, no conflict, and repair cost == 0 -> let vanilla add Mending normally
            return ActionResult.PASS;
        });
    }

    /** 1.21.8: check presence of a specific enchantment key on the component. */
    public static boolean containsEnchant(ItemEnchantmentsComponent comp, RegistryKey<Enchantment> key) {
        if (comp.isEmpty()) return false;
        for (Entry<RegistryEntry<Enchantment>> e : comp.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(key)) return true;
        }
        return false;
    }
}
