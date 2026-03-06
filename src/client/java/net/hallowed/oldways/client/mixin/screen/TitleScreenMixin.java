package net.hallowed.oldways.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

// Priority 400 ensures this runs LATE, overwriting ModMenu's changes
@Mixin(value = TitleScreen.class, priority = 400)
public abstract class TitleScreenMixin {

    @Shadow @Nullable private RealmsNotificationsScreen realmsNotificationGui;

    @Unique private static final int V_SPACING = 4;
    @Unique private static final Identifier OW$PHASE = Identifier.of("old-ways", "title_buttons_late");
    @Unique private static boolean OW$afterInitHooked = false;


    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"
            ),
            index = 1
    )
    private String oldways$stripFabricModded(String original) {
        return "Minecraft " + net.minecraft.SharedConstants.getGameVersion().name();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void oldways$hookAfterInitAndKillNotifier(CallbackInfo ci) {
        if (!SettingsPrefs.get().realmsButtons) {
            this.realmsNotificationGui = null;
        }

        if (!OW$afterInitHooked) {
            OW$afterInitHooked = true;
            ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, OW$PHASE);
            ScreenEvents.AFTER_INIT.register(OW$PHASE, (mc, screen, w, h) -> {
                if (!(screen instanceof TitleScreen)) return;

                var prefs = SettingsPrefs.get();
                List<ClickableWidget> buttons = Screens.getButtons(screen);

                if (!prefs.accessibilityButton) {
                    for (Iterator<ClickableWidget> it = buttons.iterator(); it.hasNext();) {
                        ClickableWidget wgt = it.next();
                        if (wgt.getMessage().getString().toLowerCase().contains("access")) {
                            it.remove();
                            if (wgt instanceof TextIconButtonWidget ti) { ti.visible = false; ti.active = false; }
                        }
                    }
                }

                if (prefs.realmsButtons) return;

                final Text REALMS    = Text.translatable("menu.online");
                final Text COPYRIGHT = Text.translatable("title.credits");

                ClickableWidget realmsBtn = null;
                for (ClickableWidget wgt : buttons) {
                    if (wgt.getMessage().equals(REALMS)) { realmsBtn = wgt; break; }
                }

                if (realmsBtn == null) return;

                final int centerLeft  = (screen.width - 200) / 2;
                final int centerRight = centerLeft + 200;

                final int delta = realmsBtn.getHeight() + V_SPACING;
                final int cutY  = realmsBtn.getY();

                for (ClickableWidget wgt : buttons) {
                    if (wgt == realmsBtn || !wgt.visible) continue;
                    if (wgt.getMessage().equals(COPYRIGHT)) continue;

                    boolean inCenterColumn = (wgt.getX() <= centerRight) && (wgt.getX() + wgt.getWidth() >= centerLeft);
                    boolean isSmallIcon    = (wgt instanceof TextIconButtonWidget) && wgt.getWidth() <= 22 && wgt.getHeight() <= 22;

                    if (wgt.getY() >= cutY && (inCenterColumn || isSmallIcon)
                            && (wgt instanceof ButtonWidget || wgt instanceof TextIconButtonWidget)) {
                        wgt.setY(wgt.getY() - delta);
                    }
                }
                buttons.remove(realmsBtn);
            });
        }
    }
}