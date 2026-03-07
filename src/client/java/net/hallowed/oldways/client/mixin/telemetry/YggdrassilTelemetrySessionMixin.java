package net.hallowed.oldways.client.mixin.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.yggdrasil.YggdrassilTelemetrySession;
import net.hallowed.oldways.client.util.SettingsPrefs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(YggdrassilTelemetrySession.class)
public class YggdrassilTelemetrySessionMixin {

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true)
    private void oldways$forceDisabled(CallbackInfoReturnable<Boolean> cir) {
        if (SettingsPrefs.get().telemetryOff) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "createNewEvent", at = @At("HEAD"), cancellable = true)
    private void oldways$returnEmptyEvent(String type, CallbackInfoReturnable<TelemetryEvent> cir) {
        if (SettingsPrefs.get().telemetryOff) {
            cir.setReturnValue(TelemetryEvent.EMPTY);
        }
    }
}