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
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.screen.AnvilScreenHandler.class)
public abstract class AnvilScreenHandler extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    @Unique private boolean oldways$consumeRightOnTake = false;

    protected AnvilScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId,
                                 PlayerInventory inv, ScreenHandlerContext ctx, ForgingSlotsManager slots) {
        super(type, syncId, inv, ctx, slots);
    }

    /**
     * Run AFTER vanilla builds the result so all normal checks (applicability, conflicts, etc.)
     * already happened. We only override behavior for a *pure Mending book*.
     */
    @Inject(method = "updateResult", at = @At("TAIL"))
    private void oldways$applyMendingNerfAtTail(CallbackInfo ci) {
        oldways$consumeRightOnTake = false;

        if (!CommonConfigManager.mendingNerfEnabled()) return;

        ItemStack left  = this.getSlot(0).getStack();
        ItemStack right = this.getSlot(1).getStack();

        if (!isPureMendingBook(right)) return;  // NOT our case → leave vanilla exactly as-is

        // Ask your nerf logic what to do
        AnvilUpdateEvent event = new AnvilUpdateEvent(left, right, 0);
        ActionResult res = AnvilUpdateEvent.EVENT.invoker().update(event);

        if (res == ActionResult.FAIL) {
            // Hard-block this combine
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            this.repairItemUsage = 0;
            return;
        }

        if (res == ActionResult.CONSUME) {
            // Replace with your custom output/cost; consume the book on-take
            this.output.setStack(0, event.getOutput());
            this.levelCost.set(event.getCost());
            this.repairItemUsage = 0;
            oldways$consumeRightOnTake = true;
        }

        // PASS → keep vanilla’s result exactly (do nothing)
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

    /* ---------- helpers ---------- */

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
            else return false; // contains some other enchant → not pure Mending
        }
        return mending && count == 1;
    }
}
