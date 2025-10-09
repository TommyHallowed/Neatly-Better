package net.hallowed.oldways.client.mixin.screen;

import net.hallowed.oldways.client.screen.GameplaySettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
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
                    target = "Lnet/minecraft/client/gui/screen/option/OptionsScreen;createButton(Lnet/minecraft/text/Text;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/widget/ButtonWidget;",
                    ordinal = 8
            )
    )
    private ButtonWidget oldways$replaceTelemetryWithGameplay(OptionsScreen self, Text message, Supplier<Screen> supplier) {
        return ButtonWidget.builder(
                Text.translatable("oldways.options.gameplay.button"),
                btn -> MinecraftClient.getInstance().setScreen(new GameplaySettingsScreen(self))
        ).build();
    }

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;isTelemetryEnabledByApi()Z"
            )
    )
    private boolean oldways$skipTelemetryDisable(MinecraftClient client) {
        return true;
    }
}
