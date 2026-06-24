package net.hallowed.neatlybetter.mixin.screen;

import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {

    @ModifyArg(
            method = "createInputSlotDefinitions(Lnet/minecraft/world/item/crafting/RecipeAccess;)Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;withSlot(IIILjava/util/function/Predicate;)Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;",
                    ordinal = 2
            ),
            index = 3
    )
    private static Predicate<ItemStack> neatlybetter$allowSacsInAddition(Predicate<ItemStack> original) {
        return stack -> original.test(stack)
                || stack.is(Items.GLOW_INK_SAC)
                || stack.is(Items.INK_SAC)
                || stack.is(Items.ECHO_SHARD);
    }

    @Inject(method = "canMoveIntoInputSlots", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$isValidIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean glow = stack.is(Items.GLOW_INK_SAC);
        boolean ink  = stack.is(Items.INK_SAC);
        boolean echo = stack.is(Items.ECHO_SHARD);

        if (!glow && !ink && !echo) return;

        SmithingMenu self = (SmithingMenu)(Object)this;
        Slot template = self.getSlot(0);
        Slot baseSlot = self.getSlot(1);
        Slot addSlot  = self.getSlot(2);

        if (!template.hasItem() && baseSlot.hasItem() && !addSlot.hasItem()) {
            ItemStack base = baseSlot.getItem();
            ArmorTrim trim = base.get(DataComponents.TRIM);
            if (trim == null) return;

            boolean emissive = base.getOrDefault(ModData.EMISSIVE_TRIM, false);
            boolean pulsing = base.getOrDefault(ModData.PULSING_TRIM, false);

            boolean isArmor = base.is(ItemTags.TRIMMABLE_ARMOR);

            if ((glow && !emissive) || (echo && !pulsing && isArmor) || (ink && (emissive || pulsing))) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void neatlybetter$buildAugmentResult(CallbackInfo ci) {
        SmithingMenu self = (SmithingMenu)(Object)this;

        Slot outSlot = self.getSlot(3);
        if (outSlot.hasItem()) return;

        Slot template = self.getSlot(0);
        Slot baseSlot = self.getSlot(1);
        Slot addSlot  = self.getSlot(2);

        if (template.hasItem() || !baseSlot.hasItem() || !addSlot.hasItem()) return;

        ItemStack base = baseSlot.getItem();
        ArmorTrim trim = base.get(DataComponents.TRIM);
        if (trim == null) return;

        ItemStack add = addSlot.getItem();

        boolean emissive = base.getOrDefault(ModData.EMISSIVE_TRIM, false);
        boolean pulsing = base.getOrDefault(ModData.PULSING_TRIM, false);
        boolean isArmor = base.is(ItemTags.TRIMMABLE_ARMOR);

        if (add.is(Items.GLOW_INK_SAC) && !emissive) {
            ItemStack result = base.copy();
            result.set(ModData.EMISSIVE_TRIM, true);
            result.remove(ModData.PULSING_TRIM);
            outSlot.setByPlayer(result);
            return;
        }

        if (add.is(Items.ECHO_SHARD) && !pulsing && isArmor) {
            ItemStack result = base.copy();
            result.set(ModData.PULSING_TRIM, true);
            result.remove(ModData.EMISSIVE_TRIM);
            outSlot.setByPlayer(result);
            return;
        }

        if (add.is(Items.INK_SAC) && (emissive || pulsing)) {
            ItemStack result = base.copy();
            result.remove(ModData.EMISSIVE_TRIM);
            result.remove(ModData.PULSING_TRIM);
            outSlot.setByPlayer(result);
        }
    }
}