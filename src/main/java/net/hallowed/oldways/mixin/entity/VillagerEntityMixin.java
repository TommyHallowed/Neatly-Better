package net.hallowed.oldways.mixin.entity;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Vanilla-faithful GLOBAL curing (Fabric 1.21.8, WriteView/ReadView):
 * • BEFORE ANY CURE: prices are 100% vanilla (including demand swings and penalties).
 * • AFTER FIRST CURE of THIS villager:
 *     - If Villager.GlobalCuringPrices = true:
 *         Persist the lowest FIRST-slot price per offer and enforce it for ALL players (global + persistent).
 *     - If Villager.InfiniteCuringDiscounts = true:
 *         Subsequent cures may push that stored floor even lower.
 *       If false:
 *         The first post-cure low price is locked.
 * • Hero of the Village remains temporary & per-player. We DO NOT persist HoTV discounts.
 * • We only affect FIRST slot via vanilla 'specialPrice'; second slot stays vanilla.
 * • Zero per-tick cost.
 */
@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {

    /** Has this villager ever been cured (activates persistence/globalization)? */
    @Unique private boolean oldways$cureActivated = false;

    /** Per-offer floors for FIRST buy slot. Integer.MAX_VALUE = no floor recorded yet. */
    @Unique private int[] oldways$firstSlotFloors;

    /* -------------------- Persist state (WriteView / ReadView) -------------------- */

    // VillagerEntity has writeCustomData(WriteView) / readCustomData(ReadView) in your dump.
    // Save our activation flag + floors as a CSV string to avoid depending on getIntArray.
    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void oldways$save(WriteView view, CallbackInfo ci) {
        if (!CommonConfigManager.villagerGlobalCuringPrices()) return;
        view.putBoolean("OldWaysCureActivated", this.oldways$cureActivated);
        if (oldways$firstSlotFloors != null && oldways$firstSlotFloors.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < oldways$firstSlotFloors.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(oldways$firstSlotFloors[i]);
            }
            view.putString("OldWaysFirstSlotFloorsCsv", sb.toString());
        }
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void oldways$load(ReadView view, CallbackInfo ci) {
        if (!CommonConfigManager.villagerGlobalCuringPrices()) {
            this.oldways$cureActivated = false;
            this.oldways$firstSlotFloors = null;
            return;
        }
        this.oldways$cureActivated = view.getBoolean("OldWaysCureActivated", false);
        String csv = view.getString("OldWaysFirstSlotFloorsCsv", "");
        if (csv.isEmpty()) {
            oldways$firstSlotFloors = null;
        } else {
            String[] parts = csv.split(",");
            int[] arr = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    arr[i] = Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    arr[i] = Integer.MAX_VALUE;
                }
            }
            oldways$firstSlotFloors = arr.length == 0 ? null : arr;
        }
    }

    /* -------------------- Activate only when villager is actually cured -------------------- */

    // Correct signature from your dump:
    // public void onInteractionWith(EntityInteraction interaction, Entity entity)
    @Inject(method = "onInteractionWith", at = @At("TAIL"))
    private void oldways$onInteraction(EntityInteraction interaction, Entity actor, CallbackInfo ci) {
        if (!CommonConfigManager.villagerGlobalCuringPrices()) return;
        if (interaction == EntityInteraction.ZOMBIE_VILLAGER_CURED) { // cure happened
            this.oldways$cureActivated = true;
            // Reset floors so the first *post-cure* prices become the baseline.
            this.oldways$firstSlotFloors = null;
        }
        // Other interactions (TRADE, HURT, KILLED) remain vanilla; we don't change penalties. :contentReference[oaicite:3]{index=3}
    }

    /* -------------------- Global + persistent first-slot floors (AFTER cure) -------------------- */

    // prepareOffersFor(PlayerEntity) exists (private) and applies rep/HoTV/demand. We hook AFTER it. :contentReference[oaicite:4]{index=4}
    @Inject(method = "prepareOffersFor", at = @At("RETURN"))
    private void oldways$globalizeAndPersist(PlayerEntity viewer, CallbackInfo ci) {
        if (!CommonConfigManager.villagerGlobalCuringPrices() || !this.oldways$cureActivated) return;

        final VillagerEntity self = (VillagerEntity)(Object)this;
        final TradeOfferList offers = self.getOffers();
        if (offers == null || offers.isEmpty()) return;

        // Ensure floors array matches current offers (offers can change with profession/level)
        if (oldways$firstSlotFloors == null || oldways$firstSlotFloors.length != offers.size()) {
            oldways$firstSlotFloors = new int[offers.size()];
            Arrays.fill(oldways$firstSlotFloors, Integer.MAX_VALUE);
        }

        final boolean allowFurtherLowering = CommonConfigManager.villagerInfiniteCuringDiscounts();
        final boolean viewerHasHoTV = viewer.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);

        for (int i = 0; i < offers.size(); i++) {
            TradeOffer offer = offers.get(i);
            int currentDisplayedFirst = offer.getDisplayedFirstBuyItem().getCount(); // vanilla-computed (rep + HoTV + demand)

            // 1) Record a new floor ONLY if:
            //    - InfiniteCuringDiscounts allows it (or we don't have one yet),
            //    - AND the viewer does NOT have HoTV (do not persist temporary HoTV discounts).
            int prevFloor = oldways$firstSlotFloors[i];
            int newFloor = prevFloor;

            if (!viewerHasHoTV) {
                if (allowFurtherLowering) {
                    newFloor = Math.min(prevFloor, currentDisplayedFirst);
                } else if (prevFloor == Integer.MAX_VALUE) {
                    // lock-in mode: first post-cure vanilla price we see becomes the floor
                    newFloor = currentDisplayedFirst;
                }
            }
            oldways$firstSlotFloors[i] = newFloor;

            // 2) Enforce the floor (FIRST slot only; second slot remains vanilla)
            if (newFloor != Integer.MAX_VALUE && currentDisplayedFirst > newFloor) {
                offer.increaseSpecialPrice(-(currentDisplayedFirst - newFloor));
            }
        }
        // No call to private sendOffersToCustomer(); vanilla restock/refresh handles syncing. :contentReference[oaicite:5]{index=5}
    }

    /* -------------------- Clear floors when offers are regenerated -------------------- */

    // setVillagerData(VillagerData) nulls offers when profession changes in your dump → clear our cache. :contentReference[oaicite:6]{index=6}
    @Inject(method = "setVillagerData(Lnet/minecraft/village/VillagerData;)V", at = @At("HEAD"))
    private void oldways$resetOnVillagerDataChange(VillagerData data, CallbackInfo ci) {
        this.oldways$firstSlotFloors = null;
        // Keep oldways$cureActivated: a cured villager that changes jobs should still be "activation true".
    }
}
