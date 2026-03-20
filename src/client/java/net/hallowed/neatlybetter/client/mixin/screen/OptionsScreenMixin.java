package net.hallowed.neatlybetter.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.Screens;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void neatlybetter$lockTelemetryButton(CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.telemetryOff.get()) return;

        final Component TELEMETRY = Component.translatable("options.telemetry");

        for (AbstractWidget widget : Screens.getButtons((OptionsScreen)(Object)this)) {
            if (widget.getMessage().equals(TELEMETRY)) {
                widget.active = false;
                break;
            }
        }
    }
}
