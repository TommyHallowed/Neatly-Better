package net.hallowed.oldways.client.mixin.ui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Treat all resource packs as compatible (hides warnings and enables selection).
 * This replaces the scattered redirects with one centralized override.
 */
@Mixin(net.minecraft.resource.ResourcePackCompatibility.class)
public abstract class ResourcePackCompatibility {

    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private void oldways$alwaysCompatible(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
