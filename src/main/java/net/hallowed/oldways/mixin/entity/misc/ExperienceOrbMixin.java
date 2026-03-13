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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    // ═══════════════════════════════════════════════════════════════════
    //  Configurable Constants
    // ═══════════════════════════════════════════════════════════════════

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

    /**
     * Maximum number of extra orbs to collect per burst pickup.
     * When one orb is picked up, other orbs already touching the player
     * are collected instantly instead of waiting for vanilla's 2-tick delay.
     * 0 = burst pickup disabled. 50 = up to 50 extra orbs per burst.
     */
    @Unique
    private static final int MAX_BURST_ORBS = 50;

    // ─── Re-entrancy guard for burst pickup ────────────────────────
    @Unique
    private static boolean oldways$inBurst = false;

    // ═══════════════════════════════════════════════════════════════════
    //  Feature 1: Inventory Mending
    // ═══════════════════════════════════════════════════════════════════

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
        if (original.isPresent()) return original;

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

    // ═══════════════════════════════════════════════════════════════════
    //  Feature 2: Custom XP-to-Durability Ratio
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Wraps the call to EnchantmentHelper.modifyDurabilityToRepairFromXp()
     * to apply a custom conversion ratio instead of the vanilla enchantment-driven
     * calculation.

     * Uses @WrapOperation for maximum mod compatibility — properly chains with
     * other mods wrapping the same call.

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
        if (XP_TO_DURABILITY_MULTIPLIER == 2.0) {
            return original.call(level, stack, xpAmount);
        }
        return Math.max(0, (int) (xpAmount * XP_TO_DURABILITY_MULTIPLIER));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Feature 3: Burst XP Orb Pickup
    // ═══════════════════════════════════════════════════════════════════

    /**
     * When an XP orb is picked up, instantly collects other orbs that are
     * already touching the player — eliminates vanilla's 2-tick delay
     * between picking up orbs that arrived simultaneously.

     * Collision-based only (no radius expansion) to preserve the vanilla
     * feel of orbs flying toward the player. Only orbs that have actually
     * reached the player get collected.

     * Uses a static re-entrancy guard to prevent infinite recursion when
     * calling playerTouch on burst orbs.
     */
    @Inject(method = "playerTouch", at = @At("TAIL"))
    private void oldways$burstPickup(Player player, CallbackInfo ci) {
        // Skip if already in a burst, disabled, or client-side
        if (oldways$inBurst) return;
        if (MAX_BURST_ORBS <= 0) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        try {
            oldways$inBurst = true;

            ExperienceOrb self = (ExperienceOrb) (Object) this;
            AABB playerBox = player.getBoundingBox();

            // Find all other XP orbs currently colliding with the player
            List<ExperienceOrb> collidingOrbs = serverLevel.getEntities(
                    EntityTypeTest.forClass(ExperienceOrb.class),
                    playerBox,
                    orb -> orb.isAlive() && orb != self
            );

            if (collidingOrbs.isEmpty()) return;

            int picked = 0;
            for (ExperienceOrb orb : collidingOrbs) {
                if (picked >= MAX_BURST_ORBS) break;

                // Reset the pickup delay so vanilla processes this orb immediately
                player.takeXpDelay = 0;

                // Trigger vanilla pickup path (preserves Mending, XP award, sounds, etc.)
                orb.playerTouch(player);

                picked++;
            }
        } finally {
            oldways$inBurst = false;
        }
    }
}
