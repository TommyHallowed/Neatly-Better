package net.hallowed.neatlybetter.client.mixin.ui;

import net.minecraft.server.packs.repository.PackCompatibility;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackCompatibility.class)
public abstract class ResourcePackCompatibilityMixin {

    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$alwaysCompatible(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
