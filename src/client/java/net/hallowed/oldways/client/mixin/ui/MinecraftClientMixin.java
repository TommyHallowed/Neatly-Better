package net.hallowed.oldways.client.mixin.ui;


import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();
    @Inject(method = "isTelemetryEnabledByApi", at = @At("HEAD"), cancellable = true)
    private void oldways$isTelemetryEnabledByApi(CallbackInfoReturnable<Boolean> cir) {
        if (!OW$prefs.telemetryOff) return;
        cir.setReturnValue(false);
    }
}