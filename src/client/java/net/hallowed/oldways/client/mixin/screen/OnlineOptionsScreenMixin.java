package net.hallowed.oldways.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OnlineOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(OnlineOptionsScreen.class)
public abstract class OnlineOptionsScreenMixin {

    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Inject(method = "init", at = @At("RETURN"))
    private void oldways$hideRealmsNotifications(CallbackInfo ci) {
        if (!OW$prefs.realmsButtons) return;
        Screen self = (Screen)(Object)this;
        List<ClickableWidget> buttons = Screens.getButtons(self);

        final Text REALMS_NOTIF = Text.translatable("options.realmsNotifications");
        final Text REALMS_NOTIF_TITLE = Text.translatable("options.realmsNotifications");

        buttons.removeIf(b ->
                b.getMessage().equals(REALMS_NOTIF) ||
                        b.getMessage().equals(REALMS_NOTIF_TITLE) ||
                        b.getMessage().getString().toLowerCase().contains("realms")
        );
    }
}
