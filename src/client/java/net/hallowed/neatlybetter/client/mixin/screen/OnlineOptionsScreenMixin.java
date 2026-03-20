package net.hallowed.neatlybetter.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.Screens;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OnlineOptionsScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(OnlineOptionsScreen.class)
public abstract class OnlineOptionsScreenMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void neatlybetter$hideRealmsNotifications(CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.realmsButtons.get()) return;
        Screen self = (Screen)(Object)this;
        List<AbstractWidget> buttons = Screens.getButtons(self);

        final Component REALMS_NOTIF = Component.translatable("options.realmsNotifications");
        final Component REALMS_NOTIF_TITLE = Component.translatable("options.realmsNotifications");

        buttons.removeIf(b ->
                b.getMessage().equals(REALMS_NOTIF) ||
                        b.getMessage().equals(REALMS_NOTIF_TITLE) ||
                        b.getMessage().getString().toLowerCase().contains("realms")
        );
    }
}
