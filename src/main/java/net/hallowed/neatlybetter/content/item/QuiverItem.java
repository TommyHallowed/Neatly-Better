package net.hallowed.neatlybetter.content.item;

import net.hallowed.neatlybetter.content.component.QuiverContents;
import net.hallowed.neatlybetter.tooltip.QuiverTooltip;
import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class QuiverItem extends Item {

    private static final int BAR_COLOR      = ARGB.colorFromFloat(1.0F, 0.80F, 0.50F, 0.10F);
    private static final int BAR_COLOR_FULL = ARGB.colorFromFloat(1.0F, 1.00F, 0.30F, 0.10F);

    public QuiverItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, @NonNull Slot slot, @NonNull ClickAction action, @NonNull Player player) {
        QuiverContents initial = self.get(ModData.QUIVER_CONTENTS);
        if (initial == null) return false;

        ItemStack other = slot.getItem();
        QuiverContents.Mutable contents = new QuiverContents.Mutable(initial);

        if (action == ClickAction.PRIMARY && !other.isEmpty()) {
            if (contents.tryTransfer(slot, player) > 0) playInsertSound(player);
            else playInsertFailSound(player);
            self.set(ModData.QUIVER_CONTENTS, contents.toImmutable());
            broadcastChanges(player);
            return true;
        }

        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            ItemStack removed = contents.removeOne();
            if (removed != null) {
                ItemStack remainder = slot.safeInsert(removed);
                if (!remainder.isEmpty()) contents.tryInsert(remainder);
                else playRemoveOneSound(player);
                self.set(ModData.QUIVER_CONTENTS, contents.toImmutable());
                broadcastChanges(player);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            @NonNull ItemStack self, @NonNull ItemStack other, @NonNull Slot slot,
            @NonNull ClickAction action, @NonNull Player player, @NonNull SlotAccess cursor) {

        if (action == ClickAction.PRIMARY && other.isEmpty()) {
            return false;
        }

        QuiverContents initial = self.get(ModData.QUIVER_CONTENTS);
        if (initial == null) return false;

        QuiverContents.Mutable contents = new QuiverContents.Mutable(initial);

        if (action == ClickAction.PRIMARY && !other.isEmpty()) {
            if (slot.allowModification(player) && contents.tryInsert(other) > 0) playInsertSound(player);
            else playInsertFailSound(player);
            self.set(ModData.QUIVER_CONTENTS, contents.toImmutable());
            broadcastChanges(player);
            return true;
        }

        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            if (slot.allowModification(player)) {
                ItemStack removed = contents.removeOne();
                if (removed != null) {
                    playRemoveOneSound(player);
                    cursor.set(removed);
                    self.set(ModData.QUIVER_CONTENTS, contents.toImmutable());
                    broadcastChanges(player);
                    return true;
                }
            }
        }

        return false;
    }

    public static void toggleSelectedItem(ItemStack stack, int idx) {
        QuiverContents initial = stack.get(ModData.QUIVER_CONTENTS);
        if (initial == null) return;
        QuiverContents.Mutable mutable = new QuiverContents.Mutable(initial);
        mutable.toggleSelectedItem(idx);
        stack.set(ModData.QUIVER_CONTENTS, mutable.toImmutable());
    }

    public static int getSelectedItemIndex(ItemStack stack) {
        return stack.getOrDefault(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY).getSelectedItemIndex();
    }

    public static @Nullable ItemStackTemplate getSelectedItem(ItemStack stack) {
        return stack.getOrDefault(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY).getSelectedItem();
    }

    @Override public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity entity) { return 200; }
    @Override public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack stack) { return ItemUseAnimation.BUNDLE; }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !stack.getOrDefault(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float f = stack.getOrDefault(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY).fullness();
        return Math.min(1 + (int)(f * 12), 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = stack.getOrDefault(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY).fullness();
        return f >= 1.0f ? BAR_COLOR_FULL : BAR_COLOR;
    }

    @Override
    public @NonNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        QuiverContents contents = stack.get(ModData.QUIVER_CONTENTS);
        if (contents == null || contents.isEmpty()) return Optional.empty();
        return Optional.of(new QuiverTooltip(contents));
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        QuiverContents contents = entity.getItem().get(ModData.QUIVER_CONTENTS);
        if (contents != null) {
            entity.getItem().set(ModData.QUIVER_CONTENTS, QuiverContents.EMPTY);
            ItemUtils.onContainerDestroyed(entity, contents.itemCopyStream());
        }
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    private static void playInsertSound(Entity e) {
        e.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + e.level().getRandom().nextFloat() * 0.4F);
    }
    private static void playInsertFailSound(Entity e) {
        e.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }
    private static void playRemoveOneSound(Entity e) {
        e.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + e.level().getRandom().nextFloat() * 0.4F);
    }
    private static void broadcastChanges(Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        menu.slotsChanged(player.getInventory());
    }
}