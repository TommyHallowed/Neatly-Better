package net.hallowed.neatlybetter.client.mixin.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "isAllowedInPortal", at = @At("HEAD"), cancellable = true)
    private void nt$allowContainerInPortal(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof AbstractContainerScreen) {
            cir.setReturnValue(true);
        }
    }
}