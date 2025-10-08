package net.hallowed.oldways.mixin.entity.passive;

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

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {

    @Unique private boolean oldways$cureActivated = false;

    @Unique private int[] oldways$firstSlotFloors;

    /* -------------------- Persist state (WriteView / ReadView) -------------------- */

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

    @Inject(method = "onInteractionWith", at = @At("TAIL"))
    private void oldways$onInteraction(EntityInteraction interaction, Entity actor, CallbackInfo ci) {
        if (!CommonConfigManager.villagerGlobalCuringPrices()) return;
        if (interaction == EntityInteraction.ZOMBIE_VILLAGER_CURED) {
            this.oldways$cureActivated = true;
            this.oldways$firstSlotFloors = null;
        }
    }

    /* -------------------- Global + persistent first-slot floors (AFTER cure) -------------------- */

    @Inject(method = "prepareOffersFor", at = @At("RETURN"))
    private void oldways$globalizeAndPersist(PlayerEntity viewer, CallbackInfo ci) {
        if (!CommonConfigManager.villagerGlobalCuringPrices() || !this.oldways$cureActivated) return;

        final VillagerEntity self = (VillagerEntity)(Object)this;
        final TradeOfferList offers = self.getOffers();
        if (offers == null || offers.isEmpty()) return;

        if (oldways$firstSlotFloors == null || oldways$firstSlotFloors.length != offers.size()) {
            oldways$firstSlotFloors = new int[offers.size()];
            Arrays.fill(oldways$firstSlotFloors, Integer.MAX_VALUE);
        }

        final boolean allowFurtherLowering = CommonConfigManager.villagerInfiniteCuringDiscounts();
        final boolean viewerHasHoTV = viewer.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);

        for (int i = 0; i < offers.size(); i++) {
            TradeOffer offer = offers.get(i);
            int currentDisplayedFirst = offer.getDisplayedFirstBuyItem().getCount();

            int prevFloor = oldways$firstSlotFloors[i];
            int newFloor = prevFloor;

            if (!viewerHasHoTV) {
                if (allowFurtherLowering) {
                    newFloor = Math.min(prevFloor, currentDisplayedFirst);
                } else if (prevFloor == Integer.MAX_VALUE) {
                    newFloor = currentDisplayedFirst;
                }
            }
            oldways$firstSlotFloors[i] = newFloor;

            if (newFloor != Integer.MAX_VALUE && currentDisplayedFirst > newFloor) {
                offer.increaseSpecialPrice(-(currentDisplayedFirst - newFloor));
            }
        }
    }

    /* -------------------- Clear floors when offers are regenerated -------------------- */

    @Inject(method = "setVillagerData(Lnet/minecraft/village/VillagerData;)V", at = @At("HEAD"))
    private void oldways$resetOnVillagerDataChange(VillagerData data, CallbackInfo ci) {
        this.oldways$firstSlotFloors = null;
    }
}
