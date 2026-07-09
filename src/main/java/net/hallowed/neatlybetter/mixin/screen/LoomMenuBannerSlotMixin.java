package net.hallowed.neatlybetter.mixin.screen;

import com.mojang.logging.LogUtils;

import net.hallowed.neatlybetter.util.CarpetLoomSupport;

import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.LoomMenu$3")
public abstract class LoomMenuBannerSlotMixin {

    @Unique
    private static final Logger neatlybetter$LOGGER = LogUtils.getLogger();

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$allowCarpets(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetLoomSupport.isCarpetItem(itemStack)) {
            neatlybetter$LOGGER.debug("[NeatlyBetter/LoomCarpet] banner slot accepted carpet: {}", itemStack);
            cir.setReturnValue(true);
        }
    }
}