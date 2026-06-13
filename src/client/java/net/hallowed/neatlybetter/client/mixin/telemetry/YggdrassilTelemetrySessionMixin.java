package net.hallowed.neatlybetter.client.mixin.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.yggdrasil.YggdrassilTelemetrySession;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = YggdrassilTelemetrySession.class, remap = false)
public class YggdrassilTelemetrySessionMixin {

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true, remap = false)
    private void neatlybetter$forceDisabled(CallbackInfoReturnable<Boolean> cir) {
        if (NTClientConfig.CONFIG.telemetryOff.get()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "createNewEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private void neatlybetter$returnEmptyEvent(String type, CallbackInfoReturnable<TelemetryEvent> cir) {
        if (NTClientConfig.CONFIG.telemetryOff.get()) {
            cir.setReturnValue(TelemetryEvent.EMPTY);
        }
    }
}