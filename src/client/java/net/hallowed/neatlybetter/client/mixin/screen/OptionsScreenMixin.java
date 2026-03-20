package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.client.screen.GameplaySettingsScreen;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void neatlybetter$lockTelemetryButton(CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.telemetryOff.get()) return;

        final Component TELEMETRY = Component.translatable("options.telemetry");

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;allowsTelemetry()Z"
            )
    )
    private boolean neatlybetter$skipTelemetryDisable(Minecraft client) {
        return true;
    }
}
