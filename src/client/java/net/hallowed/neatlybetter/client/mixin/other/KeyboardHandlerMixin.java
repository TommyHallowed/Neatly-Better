package net.hallowed.neatlybetter.client.mixin.other;

import net.hallowed.neatlybetter.client.util.PortalInventoryGuard;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void neatlybetter$markInputActive(CallbackInfo ci) {
        PortalInventoryGuard.isInputActive = true;
    }

    @Inject(method = "keyPress", at = @At("RETURN"))
    private void neatlybetter$clearInputActive(CallbackInfo ci) {
        PortalInventoryGuard.isInputActive = false;
    }
}