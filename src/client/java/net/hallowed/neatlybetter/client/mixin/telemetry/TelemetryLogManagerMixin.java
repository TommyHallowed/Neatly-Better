package net.hallowed.neatlybetter.client.mixin.telemetry;

import net.hallowed.neatlybetter.client.util.SettingsPrefs;

import net.minecraft.client.telemetry.TelemetryLogManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(TelemetryLogManager.class)
public class TelemetryLogManagerMixin {

    @Inject(method = "open", at = @At("HEAD"), cancellable = true)
    private static void neatlybetter$stopFolderCreation(Path path, CallbackInfoReturnable<CompletableFuture<Optional<TelemetryLogManager>>> cir) {
        if (SettingsPrefs.get().telemetryOff) {
            cir.setReturnValue(CompletableFuture.completedFuture(Optional.empty()));
        }
    }
}