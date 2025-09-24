package net.hallowed.oldways.client.mixin;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OnlineOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Hide the “Realms Notifications” option on the Online Options screen (1.21.8). */
@Mixin(OnlineOptionsScreen.class)
public abstract class OnlineOptionsScreenMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void oldways$hideRealmsNotifications(CallbackInfo ci) {
        Screen self = (Screen)(Object)this;
        List<ClickableWidget> buttons = Screens.getButtons(self);

        final Text REALMS_NOTIF = Text.translatable("options.realmsNotifications");
        final Text REALMS_NOTIF_TITLE = Text.translatable("options.realmsNotifications.title");

        // Some mappings use the title key for the same toggle; remove either
        buttons.removeIf(b ->
                b.getMessage().equals(REALMS_NOTIF) ||
                        b.getMessage().equals(REALMS_NOTIF_TITLE) ||
                        b.getMessage().getString().toLowerCase().contains("realms")
        );
    }
}
