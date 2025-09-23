package net.hallowed.oldways.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.hallowed.oldways.api.events.AnvilUpdateEvent;
import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    @Unique private boolean hallowed$shouldConsumeRightBook = false;

    protected AnvilScreenHandlerMixin(
            @Nullable ScreenHandlerType<?> type,
            int syncId,
            PlayerInventory inv,
            ScreenHandlerContext ctx,
            ForgingSlotsManager slots
    ) {
        super(type, syncId, inv, ctx, slots);
    }

    @Inject(
            method = "updateResult()V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/screen/AnvilScreenHandler;repairItemUsage:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0
            ),
            cancellable = true
    )
    private void oldWays$injectUpdate(CallbackInfo ci,
                                      @Local(ordinal = 0) ItemStack left,
                                      @Local(ordinal = 2) ItemStack right) {
        // If the feature is disabled, let vanilla handle everything.
        if (!CommonConfigManager.mendingNerfEnabled()) {
            this.hallowed$shouldConsumeRightBook = false;
            return;
        }

        this.hallowed$shouldConsumeRightBook = false; // reset flag each update

        AnvilUpdateEvent event = new AnvilUpdateEvent(left, right, 0);
        ActionResult res = AnvilUpdateEvent.EVENT.invoker().update(event);

        if (res == ActionResult.FAIL) {
            // IMPORTANT: clear stale output & reset costs when we hard-block the recipe
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            this.repairItemUsage = 0;
            this.hallowed$shouldConsumeRightBook = false;
            ci.cancel();
            return;
        }

        if (res == ActionResult.CONSUME) {
            this.output.setStack(0, event.getOutput());
            this.levelCost.set(event.getCost());
            this.repairItemUsage = 0;                  // stop vanilla material consumption
            this.hallowed$shouldConsumeRightBook = true; // consume book on-take
            ci.cancel();
        }
        // else PASS → vanilla proceeds normally
    }

    // Consume exactly one right-slot book when our custom recipe was used.
    @Inject(method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"))
    private void oldWays$consumeRight(PlayerEntity player, ItemStack taken, CallbackInfo ci) {
        if (!hallowed$shouldConsumeRightBook) return;

        ItemStack right = this.getSlot(1).getStack(); // right input slot
        if (!right.isEmpty()) {
            right.decrement(1);
            this.getSlot(1).setStack(right.isEmpty() ? ItemStack.EMPTY : right);
        }
        hallowed$shouldConsumeRightBook = false;
    }
}
