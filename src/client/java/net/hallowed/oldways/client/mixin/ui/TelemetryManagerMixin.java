package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.session.telemetry.TelemetryManager;
import net.minecraft.client.session.telemetry.TelemetrySender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TelemetryManager.class)
public class TelemetryManagerMixin {
    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();
    @Inject(method = "computeSender", at = @At("HEAD"), cancellable = true)
    private void oldways$disableTelemetry(CallbackInfoReturnable<TelemetrySender> cir) {
        if (!OW$prefs.telemetryOff) return;
        cir.setReturnValue(TelemetrySender.NOOP);
    }
}
