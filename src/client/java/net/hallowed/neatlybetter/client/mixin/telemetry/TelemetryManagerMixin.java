package net.hallowed.neatlybetter.client.mixin.telemetry;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryEventSender;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTelemetryManager.class)
public class TelemetryManagerMixin {

    @Inject(method = "createEventSender", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$disableTelemetry(CallbackInfoReturnable<TelemetryEventSender> cir) {
        if (!NTClientConfig.CONFIG.telemetryOff.get()) return;
        cir.setReturnValue(TelemetryEventSender.DISABLED);
    }
}
