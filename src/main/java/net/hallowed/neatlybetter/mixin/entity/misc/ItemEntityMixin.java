package net.hallowed.neatlybetter.mixin.entity.misc;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow public abstract ItemStack getItem();

    @Inject(method = "fireImmune()Z", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$charcoalIsFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem().is(Items.CHARCOAL)) {
            cir.setReturnValue(true);
        }
    }
}