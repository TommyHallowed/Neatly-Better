package net.hallowed.oldways.client.mixin.telemetry;

import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryEventSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTelemetryManager.class)
public class TelemetryManagerMixin {
    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();
    @Inject(method = "createEventSender", at = @At("HEAD"), cancellable = true)
    private void oldways$disableTelemetry(CallbackInfoReturnable<TelemetryEventSender> cir) {
        if (!OW$prefs.telemetryOff) return;
        cir.setReturnValue(TelemetryEventSender.DISABLED);
    }
}
