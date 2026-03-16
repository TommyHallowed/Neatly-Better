package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.client.screen.GameplaySettingsScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/options/OptionsScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;",
                    ordinal = 8
            )
    )
    private Button neatlybetter$replaceTelemetryWithGameplay(OptionsScreen self, Component message, Supplier<Screen> supplier) {
        return Button.builder(
                Component.translatable("options.gameplay.button"),
                btn -> Minecraft.getInstance().setScreen(new GameplaySettingsScreen(self))
        ).build();
    }

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
