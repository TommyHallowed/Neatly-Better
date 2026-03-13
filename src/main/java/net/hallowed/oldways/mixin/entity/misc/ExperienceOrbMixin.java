package net.hallowed.oldways.mixin.entity.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    // ─── Configurable Values ───────────────────────────────────────────────────

    /**
     * XP-to-durability multiplier applied during Mending repair.
     * Vanilla default: 2.0 (1 XP = 2 durability restored).
     * Examples:
     *   1.0 → 1 XP = 1 durability  (half as efficient)
     *   2.0 → 1 XP = 2 durability  (vanilla)
     *   3.0 → 1 XP = 3 durability  (50% more efficient)
     *   4.0 → 1 XP = 4 durability  (double efficiency)
     */
    @Unique
    private static final double XP_TO_DURABILITY_MULTIPLIER = 4.0;

    /**
     * Controls which inventory slots are scanned for Mending items.
     * false → scans the entire player inventory (default)
     * true  → only scans the hotbar (slots 0–8)
     */
    @Unique
    private static final boolean MEND_HOTBAR_ONLY = false;

    // ─── Inventory Mending ─────────────────────────────────────────────────────

    /**
     * Extends Mending to repair items anywhere in the player's inventory,
     * not just equipped armor and held items. Vanilla only checks equipment
     * slots (armor + hands); this fallback scans the full inventory when
     * no equipped Mending item needs repair.

     * Priority: equipped items always take precedence (vanilla behavior).
     * Only when no equipped item qualifies does the inventory scan activate.
     */
    @ModifyVariable(
            method = "repairPlayerItems",
            at = @At(value = "STORE"),
            ordinal = 0
    )
    private Optional<EnchantedItemInUse> oldways$extendMendingToInventory(
            Optional<EnchantedItemInUse> original,
            ServerPlayer serverPlayer,
            int xpAmount) {
        // Equipped Mending item found — use vanilla behavior
        if (original.isPresent()) return original;

        // Scan inventory for damaged items with Mending
        List<EnchantedItemInUse> candidates = new ArrayList<>();
        int size = MEND_HOTBAR_ONLY ? 9 : serverPlayer.getInventory().getContainerSize();

        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = serverPlayer.getInventory().getItem(slot);
            if (!stack.isDamaged()) continue;

            ItemEnchantments enchantments = stack.getOrDefault(
                    DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                if (entry.getKey().value().effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP)) {
                    candidates.add(new EnchantedItemInUse(stack, null, serverPlayer));
                    break;
                }
            }
        }

        return Util.getRandomSafe(candidates, serverPlayer.getRandom());
    }

    // ─── Custom XP-to-Durability Ratio ─────────────────────────────────────────

    /**
     * Wraps the call to EnchantmentHelper.modifyDurabilityToRepairFromXp()
     * to apply a custom conversion ratio instead of the vanilla enchantment-driven
     * calculation.

     * Uses @WrapOperation (MixinExtras, bundled with Fabric Loader 0.15+) for
     * maximum mod compatibility — properly chains with other mods wrapping the
     * same call, unlike @Redirect which would conflict.

     * When XP_TO_DURABILITY_MULTIPLIER == 2.0, delegates to vanilla unchanged.
     */
    @WrapOperation(
            method = "repairPlayerItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDurabilityToRepairFromXp(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"
            )
    )
    private int oldways$customXpToDurability(
            ServerLevel level, ItemStack stack, int xpAmount,
            Operation<Integer> original) {
        // Default multiplier — pass through to vanilla / other mods in the chain
        if (XP_TO_DURABILITY_MULTIPLIER == 2.0) {
            return original.call(level, stack, xpAmount);
        }
        // Custom multiplier — override the enchantment-based calculation
        return Math.max(0, (int) (xpAmount * XP_TO_DURABILITY_MULTIPLIER));
    }
}
