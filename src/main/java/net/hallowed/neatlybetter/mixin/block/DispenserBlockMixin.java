package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.hallowed.neatlybetter.content.feature.CauldronDispenseBehavior;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {

    @ModifyReturnValue(
            method = "getDispenseMethod(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;",
            at = @At("RETURN")
    )
    private DispenseItemBehavior neatlybetter$wrapCauldronBehavior(
            DispenseItemBehavior original, Level level, ItemStack stack) {

        if (stack.getItem() instanceof BucketItem
                || stack.getItem() instanceof SolidBucketItem) {
            return new CauldronDispenseBehavior(original);
        }
        return original;
    }
}
