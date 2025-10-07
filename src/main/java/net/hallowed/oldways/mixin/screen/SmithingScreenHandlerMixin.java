package net.hallowed.oldways.mixin.screen;

import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin {

    @ModifyArg(
            method = "createForgingSlotsManager(Lnet/minecraft/recipe/RecipeManager;)Lnet/minecraft/screen/slot/ForgingSlotsManager;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;input(IIILjava/util/function/Predicate;)Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;",
                    ordinal = 2
            ),
            index = 3
    )
    private static Predicate<ItemStack> oldways$allowSacsInAddition(Predicate<ItemStack> original) {
        return stack -> original.test(stack)
                || stack.isOf(Items.GLOW_INK_SAC)
                || stack.isOf(Items.INK_SAC);
    }

    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private void oldways$isValidIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean glow = stack.isOf(Items.GLOW_INK_SAC);
        boolean ink  = stack.isOf(Items.INK_SAC);
        if (!glow && !ink) return;

        SmithingScreenHandler self = (SmithingScreenHandler)(Object)this;
        Slot template = self.getSlot(0);
        Slot baseSlot = self.getSlot(1);
        Slot addSlot  = self.getSlot(2);

        if (!template.hasStack() && baseSlot.hasStack() && !addSlot.hasStack()) {
            ItemStack base = baseSlot.getStack();
            ArmorTrim trim = base.get(DataComponentTypes.TRIM);
            if (trim == null) return;

            boolean emissive = base.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false);
            if ((glow && !emissive) || (ink && emissive)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void oldways$buildAugmentResult(CallbackInfo ci) {
        SmithingScreenHandler self = (SmithingScreenHandler)(Object)this;

        Slot outSlot = self.getSlot(3);
        if (outSlot.hasStack()) return;

        Slot template = self.getSlot(0);
        Slot baseSlot = self.getSlot(1);
        Slot addSlot  = self.getSlot(2);

        if (template.hasStack() || !baseSlot.hasStack() || !addSlot.hasStack()) return;

        ItemStack base = baseSlot.getStack();
        ArmorTrim trim = base.get(DataComponentTypes.TRIM);
        if (trim == null) return;

        ItemStack add = addSlot.getStack();
        boolean emissive = base.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false);

        if (add.isOf(Items.GLOW_INK_SAC) && !emissive) {
            ItemStack result = base.copy();
            result.set(ModDataComponents.EMISSIVE_TRIM, true);
            outSlot.setStack(result);
            return;
        }

        if (add.isOf(Items.INK_SAC) && emissive) {
            ItemStack result = base.copy();
            result.remove(ModDataComponents.EMISSIVE_TRIM);
            outSlot.setStack(result);
        }
    }
}
