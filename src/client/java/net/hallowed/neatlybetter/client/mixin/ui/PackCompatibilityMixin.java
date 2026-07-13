package net.hallowed.neatlybetter.client.mixin.ui;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.minecraft.server.packs.repository.PackCompatibility;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackCompatibility.class)
public abstract class PackCompatibilityMixin {

    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$alwaysCompatible(CallbackInfoReturnable<Boolean> cir) {
        if (NTClientConfig.CONFIG.resourcePackCompatibility.isTrue()) return;
        cir.setReturnValue(true);
    }
}
