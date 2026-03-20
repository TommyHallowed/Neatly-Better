package net.hallowed.neatlybetter.client.mixin.ui;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "allowsTelemetry", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$isTelemetryEnabledByApi(CallbackInfoReturnable<Boolean> cir) {
        if (!NTClientConfig.CONFIG.telemetryOff.get()) return;
        cir.setReturnValue(false);
    }
}