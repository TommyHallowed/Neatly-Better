package net.hallowed.oldways.client.mixin.ui;

import net.minecraft.resource.ResourcePackCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Treat all resource packs as compatible (hides warnings and enables selection).
 * This replaces the scattered redirects with one centralized override.
 */
@Mixin(ResourcePackCompatibility.class)
public abstract class ResourcePackCompatibilityMixin {

    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private void oldways$alwaysCompatible(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
