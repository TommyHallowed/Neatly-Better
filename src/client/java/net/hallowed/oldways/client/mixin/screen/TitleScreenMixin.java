package net.hallowed.oldways.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();
    @Shadow @Nullable private RealmsNotificationsScreen realmsNotificationGui;

    @Inject(method = "init", at = @At("RETURN"))
    private void oldways$stripButtons(CallbackInfo ci) {
        Screen self = (Screen)(Object)this;
        List<ClickableWidget> buttons = Screens.getButtons(self);

        final boolean disableRealms = !OW$prefs.realmsButtons;
        final boolean disableAccess = !OW$prefs.accessibilityButton;

        final Text REALMS = Text.translatable("menu.online");
        final Text ACCESS = Text.translatable("accessibility.onboarding.accessibility.button");

        Iterator<ClickableWidget> it = buttons.iterator();
        while (it.hasNext()) {
            ClickableWidget w = it.next();
            Text msg = w.getMessage();
            String s = msg.getString().toLowerCase();

            boolean isRealms = msg.equals(REALMS) || s.contains("realms");
            boolean isAccess = msg.equals(ACCESS) || s.contains("access");

            if ((disableRealms && isRealms) || (disableAccess && isAccess)) {
                it.remove();
                if (w instanceof TextIconButtonWidget tw) {
                    tw.visible = false;
                    tw.active = false;
                }
            }
        }

        if (disableRealms) {
            this.realmsNotificationGui = null;
        }
    }
}
