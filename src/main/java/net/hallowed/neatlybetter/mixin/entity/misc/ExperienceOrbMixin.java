package net.hallowed.neatlybetter.mixin.entity.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.hallowed.neatlybetter.config.NTServerConfig.MendingScope;
import net.hallowed.neatlybetter.config.NTServerConfig;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    // ── Config ─────────────────────────────────────────────────────────────
    @Unique private static final double  XP_TO_DURABILITY_MULTIPLIER = 4.0;
    @Unique private static final boolean MEND_HOTBAR_ONLY            = false;
    @Unique private static final int     MAX_BURST_ORBS              = 50;

    /** Set to {@code true} to enable XP-leak detection logs in the game console. */
    @Unique private static final boolean DEBUG_XP_TRACKING           = true;

    // ── Logger (near-zero cost when DEBUG_XP_TRACKING is off) ──────────────
    @Unique private static final Logger LOGGER = LoggerFactory.getLogger("neatly-better");

    // ── Re-entrancy guard for burst pickup ─────────────────────────────────
    @Unique private static boolean neatlybetter$inBurst = false;

    // ── Per-orb debug state (safe — server tick is single-threaded) ────────
    @Unique private int neatlybetter$repairDepth = 0;

    // ── Shadows ────────────────────────────────────────────────────────────
    // NOTE: 'value' is NOT a field in 1.21.1 — it's stored in entityData.
    //       Use getValue() via the helper below instead of shadowing 'value'.
    @Shadow private int count;

    /**
     * Safe accessor for the orb's XP value (backed by synched entity data,
     * not a plain field in this MC version).
     */
    @Unique
    private int neatlybetter$orbValue() {
        return ((ExperienceOrb) (Object) this).getValue();
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  DEBUG — repairPlayerItems HEAD
     *
     *  Fires once per recursion level. Vanilla recurses when leftover XP
     *  can mend another item:  remaining = xp - actualRepair * xp / durToRepair
     * ═══════════════════════════════════════════════════════════════════════ */
    @Inject(method = "repairPlayerItems", at = @At("HEAD"))
    private void neatlybetter$debugRepairHead(ServerPlayer player, int xpAmount,
                                              CallbackInfoReturnable<Integer> cir) {
        if (!DEBUG_XP_TRACKING) return;
        neatlybetter$repairDepth++;
        LOGGER.info("[neatly-better|XP] repairPlayerItems START (depth={}) | orbValue={} xpIn={} player={}",
                neatlybetter$repairDepth, neatlybetter$orbValue(), xpAmount,
                player.getScoreboardName());
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  (1) Inventory Mending — extend candidate selection to full inventory
     * ═══════════════════════════════════════════════════════════════════════ */
    @ModifyVariable(
            method  = "repairPlayerItems",
            at      = @At(value = "STORE"),
            ordinal = 0
    )
    private Optional<EnchantedItemInUse> neatlybetter$extendMendingToInventory(
            Optional<EnchantedItemInUse> original,
            ServerPlayer serverPlayer,
            int xpAmount) {

        MendingScope scope = NTServerConfig.CONFIG.mendingInventory.get();

        if (scope == MendingScope.VANILLA) {
            if (DEBUG_XP_TRACKING && original.isPresent()) {
                ItemStack s = original.get().itemStack();
                LOGGER.info("[neatly-better|XP]   (depth={}) Mending target [vanilla]: {} [dmg={}/{}]",
                        neatlybetter$repairDepth, s.getDisplayName().getString(),
                        s.getDamageValue(), s.getMaxDamage());
            }
            return original;
        }

        if (original.isPresent()) {
            if (DEBUG_XP_TRACKING) {
                ItemStack s = original.get().itemStack();
                LOGGER.info("[neatly-better|XP]   (depth={}) Mending target [vanilla]: {} [dmg={}/{}]",
                        neatlybetter$repairDepth, s.getDisplayName().getString(),
                        s.getDamageValue(), s.getMaxDamage());
            }
            return original;
        }

        // Scan inventory for Mending items that need repair
        List<EnchantedItemInUse> candidates = new ArrayList<>();
        int size = (scope == MendingScope.HOTBAR) ? 9 : serverPlayer.getInventory().getContainerSize();

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

        Optional<EnchantedItemInUse> result = Util.getRandomSafe(candidates, serverPlayer.getRandom());

        if (DEBUG_XP_TRACKING) {
            if (result.isPresent()) {
                ItemStack s = result.get().itemStack();
                LOGGER.info("[neatly-better|XP]   (depth={}) Mending target [inventory]: {} "
                                + "[dmg={}/{} candidates={}]",
                        neatlybetter$repairDepth, s.getDisplayName().getString(),
                        s.getDamageValue(), s.getMaxDamage(), candidates.size());
            } else {
                LOGGER.info("[neatly-better|XP]   (depth={}) No mending target found "
                                + "[slots={} candidates=0]",
                        neatlybetter$repairDepth, size);
            }
        }

        return result;
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  (2) Custom XP-to-Durability Ratio
     *
     *  Vanilla back-conversion is proportional:
     *      remaining = xp - actualRepair * xp / durToRepair
     *  This self-corrects for any multiplier — no separate patch needed.
     *
     *  Integer truncation in (k * i / j) always rounds DOWN, so the player
     *  keeps slightly MORE XP than the floating-point result. This is a tiny
     *  player benefit, not a leak.
     *
     *  NOTE: When multiplier != 2.0 the original EnchantmentHelper call is
     *  bypassed, so enchantment-level modifiers to repair rate won't apply.
     *  If you need to chain them, call original.call() and scale its result.
     * ═══════════════════════════════════════════════════════════════════════ */
    @WrapOperation(
            method = "repairPlayerItems",
            at     = @At(
                    value  = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;"
                            + "modifyDurabilityToRepairFromXp(Lnet/minecraft/server/level/ServerLevel;"
                            + "Lnet/minecraft/world/item/ItemStack;I)I"
            )
    )
    private int neatlybetter$customXpToDurability(
            ServerLevel level, ItemStack stack, int xpAmount,
            Operation<Integer> original) {

        double multiplier = NTServerConfig.CONFIG.mendingEfficiency.get();

        if (multiplier == 2.0) {
            int result = original.call(level, stack, xpAmount);
            if (DEBUG_XP_TRACKING) {
                neatlybetter$logDurabilityConversion(xpAmount, result, stack.getDamageValue(), "vanilla");
            }
            return result;
        }

        int result = Math.max(0, (int) (xpAmount * XP_TO_DURABILITY_MULTIPLIER));

        if (DEBUG_XP_TRACKING) {
            neatlybetter$logDurabilityConversion(xpAmount, result, stack.getDamageValue(),
                    String.format("mod %.1fx", XP_TO_DURABILITY_MULTIPLIER));

            // Degenerate case: result=0 when xpAmount > 0
            // Vanilla does:  if (k > 0 && ...) { recurse } return 0;
            // When j (durToRepair) = 0: k = min(0, dmg) = 0 → falls to return 0
            // ALL XP is consumed with ZERO repair — a true leak!
            if (result == 0 && xpAmount > 0) {
                LOGGER.warn("[neatly-better|XP]   ⚠ XP LEAK — durToRepair=0 from {}xp! "
                                + "Vanilla will return 0, silently consuming all XP with no repair. "
                                + "This happens when (int)(xp * multiplier) rounds to 0.",
                        xpAmount);
            }
        }

        return result;
    }

    /**
     * Shared debug logging for the durability conversion step.
     * Predicts what vanilla's proportional formula will compute.
     */
    @Unique
    private void neatlybetter$logDurabilityConversion(int xpIn, int durToRepair,
                                                      int itemDamage, String source) {
        int clampedRepair = Math.min(durToRepair, itemDamage);

        // Predict vanilla proportional formula: consumed = k * i / j  (integer math)
        int predictedConsumed  = (durToRepair > 0) ? (clampedRepair * xpIn / durToRepair) : xpIn;
        int predictedRemaining = xpIn - predictedConsumed;

        // True floating-point cost for comparison
        double trueCost = (XP_TO_DURABILITY_MULTIPLIER > 0)
                ? clampedRepair / XP_TO_DURABILITY_MULTIPLIER : 0;
        double truncationGap = trueCost - predictedConsumed;

        LOGGER.info("[neatly-better|XP]   (depth={}) XP->Dur [{}] | xpIn={} dur={} itemDmg={} "
                        + "repair={} | consume={} remain={} trueCost={} truncGap={}",
                neatlybetter$repairDepth, source,
                xpIn, durToRepair, itemDamage, clampedRepair,
                predictedConsumed, predictedRemaining,
                String.format("%.2f", trueCost),
                String.format("%.4f", truncationGap));

        // Truncation gap should always be >= 0 (player benefit).
        // A negative gap means the player is being overcharged — should not happen
        // with vanilla's integer-division formula, but flag it anyway.
        if (truncationGap < -0.01) {
            LOGGER.warn("[neatly-better|XP]   ⚠ UNEXPECTED OVERCHARGE — player charged "
                            + "{} more XP than the true cost warrants!",
                    String.format("%.4f", Math.abs(truncationGap)));
        }
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  DEBUG — repairPlayerItems RETURN (audit each recursion level)
     * ═══════════════════════════════════════════════════════════════════════ */
    @Inject(method = "repairPlayerItems", at = @At("RETURN"))
    private void neatlybetter$debugRepairReturn(ServerPlayer player, int xpAmount,
                                                CallbackInfoReturnable<Integer> cir) {
        if (!DEBUG_XP_TRACKING) return;

        int returned = cir.getReturnValue();
        int consumed = xpAmount - returned;

        LOGGER.info("[neatly-better|XP] repairPlayerItems END (depth={}) | xpIn={} xpOut={} "
                        + "consumed={} -> {}",
                neatlybetter$repairDepth, xpAmount, returned, consumed,
                returned > 0 ? "remaining goes to XP bar"
                        : "all consumed by mending (or no target found)");

        // Negative return = XP destroyed (should be impossible with proportional formula,
        // but another mixin or future MC change could introduce this)
        if (returned < 0) {
            LOGGER.warn("[neatly-better|XP]   ⚠ XP LEAK — return is NEGATIVE ({})! "
                    + "{} XP was silently destroyed.", returned, Math.abs(returned));
        }

        neatlybetter$repairDepth--;
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  (3) Burst XP Orb Pickup — collect nearby orbs in the same tick
     * ═══════════════════════════════════════════════════════════════════════ */
    @Inject(method = "playerTouch", at = @At("TAIL"))
    private void neatlybetter$burstPickup(Player player, CallbackInfo ci) {
        if (neatlybetter$inBurst) return;
        if (MAX_BURST_ORBS <= 0) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        try {
            neatlybetter$inBurst = true;

            ExperienceOrb self = (ExperienceOrb) (Object) this;
            AABB playerBox = player.getBoundingBox();

            List<ExperienceOrb> collidingOrbs = serverLevel.getEntities(
                    EntityTypeTest.forClass(ExperienceOrb.class),
                    playerBox,
                    orb -> orb.isAlive() && orb != self
            );

            if (collidingOrbs.isEmpty()) return;

            if (DEBUG_XP_TRACKING) {
                int totalBurstXp = 0;
                for (ExperienceOrb orb : collidingOrbs) {
                    totalBurstXp += orb.getValue();
                }
                LOGGER.info("[neatly-better|XP] === Burst START === "
                                + "triggerOrb={} (count={}) nearbyOrbs={} nearbyXp={}",
                        neatlybetter$orbValue(), count,
                        collidingOrbs.size(), totalBurstXp);
            }

            int picked      = 0;
            int skippedDead  = 0;

            for (ExperienceOrb orb : collidingOrbs) {
                if (picked >= MAX_BURST_ORBS) break;

                // Guard: orb may have been consumed by a prior iteration's playerTouch
                // (e.g. count decremented to 0 and discarded, or merged by another mod)
                if (!orb.isAlive()) {
                    skippedDead++;
                    if (DEBUG_XP_TRACKING) {
                        LOGGER.info("[neatly-better|XP]   Burst: skipped dead orb (value was {})",
                                orb.getValue());
                    }
                    continue;
                }

                player.takeXpDelay = 0;
                orb.playerTouch(player);
                picked++;
            }

            if (DEBUG_XP_TRACKING) {
                LOGGER.info("[neatly-better|XP] === Burst END === "
                                + "picked={} skippedDead={} limit={}",
                        picked, skippedDead, MAX_BURST_ORBS);
                if (skippedDead > 0) {
                    LOGGER.warn("[neatly-better|XP] ⚠ {} orb(s) died between query and "
                                    + "pickup (XP lost to merge or external removal)",
                            skippedDead);
                }
            }
        } finally {
            neatlybetter$inBurst = false;
        }
    }
}
