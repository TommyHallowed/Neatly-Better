package net.hallowed.oldways.mixin.screen;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.hallowed.oldways.api.events.AnvilUpdateEvent;
import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    @Unique private boolean oldways$consumeRightOnTake = false;
    @Unique private int oldways$cachedLevelCost = 0;

    protected AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId,
                                      PlayerInventory inv, ScreenHandlerContext ctx, ForgingSlotsManager slots) {
        super(type, syncId, inv, ctx, slots);
    }

    /* mending behaviour + 0 rename cost (merged TAIL) */
    @Inject(method = "updateResult", at = @At("TAIL"))
    private void oldways$mendingAndRename(CallbackInfo ci) {
        oldways$consumeRightOnTake = false;

        boolean mendingEnabled = CommonConfigManager.mendingNerfEnabled();
        ItemStack left  = this.getSlot(0).getStack();
        ItemStack right = this.getSlot(1).getStack();

        if (mendingEnabled && isPureMendingBook(left)) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            this.repairItemUsage = 0;
            oldways$cachedLevelCost = 0;
            return;
        }

        if (mendingEnabled && isPureMendingBook(right)) {
            AnvilUpdateEvent event = new AnvilUpdateEvent(left, right, 0);
            ActionResult res = AnvilUpdateEvent.EVENT.invoker().update(event);

            if (res == ActionResult.FAIL) {
                this.output.setStack(0, ItemStack.EMPTY);
                this.levelCost.set(0);
                this.repairItemUsage = 0;
                oldways$cachedLevelCost = 0;
                return;
            }

            if (res == ActionResult.CONSUME) {
                this.output.setStack(0, event.getOutput());
                this.levelCost.set(event.getCost());
                this.repairItemUsage = 0;
                oldways$consumeRightOnTake = true;
            }
        }

        if (oldways$isPureRename()) {
            this.levelCost.set(0);
            oldways$cachedLevelCost = 0;
        } else {
            oldways$cachedLevelCost = this.levelCost.get();
        }
    }

    @Inject(method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"))
    private void oldways$consumeRightBook(PlayerEntity player, ItemStack taken, CallbackInfo ci) {
        if (!oldways$consumeRightOnTake) return;
        ItemStack right = this.getSlot(1).getStack();
        if (right.isOf(Items.ENCHANTED_BOOK)) {
            right.decrement(1);
            this.getSlot(1).setStack(right.isEmpty() ? ItemStack.EMPTY : right);
        }
        oldways$consumeRightOnTake = false;
    }

    @Inject(method = "getLevelCost", at = @At("HEAD"), cancellable = true)
    private void oldways$getLevelCostMirror(CallbackInfoReturnable<Integer> cir) {
        if (oldways$isPureRename()) {
            cir.setReturnValue(oldways$cachedLevelCost);
        }
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    private void oldways$allowTakeWhenZeroCost(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (oldways$isPureRename()) {
            cir.setReturnValue(present);
        }
    }

    /* helpers */
    @Unique
    private boolean oldways$outputIsRenamed() {
        ItemStack input = this.getSlot(0).getStack();
        ItemStack output = this.getSlot(2).getStack();
        if (output.isEmpty()) return false;
        Text inName  = input.get(DataComponentTypes.CUSTOM_NAME);
        Text outName = output.get(DataComponentTypes.CUSTOM_NAME);
        if (inName == null && outName == null) return false;
        if (inName == null) return true;
        if (outName == null) return true;
        return !inName.equals(outName);
    }

    @Unique
    private boolean oldways$isPureRename() {
        ItemStack right = this.getSlot(1).getStack();
        return right.isEmpty() && oldways$outputIsRenamed();
    }

    @Unique
    private static boolean isPureMendingBook(ItemStack stack) {
        if (!stack.isOf(Items.ENCHANTED_BOOK)) return false;
        ItemEnchantmentsComponent stored =
                stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        boolean mending = false;
        int count = 0;
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : stored.getEnchantmentEntries()) {
            count++;
            if (e.getKey().matchesKey(Enchantments.MENDING)) mending = true;
            else return false;
        }
        return mending && count == 1;
    }

    @Unique
    private static int oldways$getKnockbackLevelFromBook(ItemStack stack) {
        if (!stack.isOf(Items.ENCHANTED_BOOK)) return 0;
        ItemEnchantmentsComponent stored =
                stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : stored.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(Enchantments.KNOCKBACK)) {
                int lvl = e.getIntValue();
                return Math.min(Math.max(lvl, 1), 2);
            }
        }
        return 0;
    }

    @Unique
    private static int oldways$getKnockbackLevelFromItem(ItemStack stack) {
        ItemEnchantmentsComponent ench =
                stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : ench.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(Enchantments.KNOCKBACK)) {
                int lvl = e.getIntValue();
                return Math.min(Math.max(lvl, 0), 2);
            }
        }
        return 0;
    }
}
