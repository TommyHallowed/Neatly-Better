package net.hallowed.neatlybetter.client.mixin.ui;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.Minecraft;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "allowsTelemetry", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$isTelemetryEnabledByApi(CallbackInfoReturnable<Boolean> cir) {
        if (!NTClientConfig.CONFIG.telemetryOff.get()) return;
        cir.setReturnValue(false);
    }

    @Unique
    private static final SoundSource[] SOURCES_TO_STOP = {
            SoundSource.RECORDS, SoundSource.WEATHER, SoundSource.BLOCKS,
            SoundSource.HOSTILE, SoundSource.NEUTRAL, SoundSource.PLAYERS,
            SoundSource.AMBIENT, SoundSource.VOICE, SoundSource.UI
    };

    @Redirect(
            method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop()V")
    )
    private void NeatlyBetter$RedirectSoundManagerStop(SoundManager instance) {
        for (SoundSource source : SOURCES_TO_STOP) {
            instance.stop(null, source);
        }
    }
}