package net.hallowed.oldways.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;

import java.util.Iterator;
import java.util.List;

// Priority 400 ensures this runs LATE, overwriting ModMenu's changes
@Mixin(value = TitleScreen.class, priority = 400)
public abstract class TitleScreenMixin {

    @Shadow @Nullable private RealmsNotificationsScreen realmsNotificationsScreen;

    @Unique private static final int V_SPACING = 4;

    // FIX: Using your environment's Identifier class
    @Unique private static final Identifier OW$PHASE = Identifier.fromNamespaceAndPath("old-ways", "title_buttons_late");
    @Unique private static boolean OW$afterInitHooked = false;

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    // FIX: Reverted the return type to 'V' (void) because Mojang's new
                    // GuiRenderState update removed the 'int' return type from drawString!
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            index = 1
    )
    private String oldways$stripFabricModded(String original) {
        return "Minecraft " + net.minecraft.SharedConstants.getCurrentVersion().name();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void oldways$hookAfterInitAndKillNotifier(CallbackInfo ci) {
        if (!SettingsPrefs.get().realmsButtons) {
            this.realmsNotificationsScreen = null;
        }

        if (!OW$afterInitHooked) {
            OW$afterInitHooked = true;
            ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, OW$PHASE);
            ScreenEvents.AFTER_INIT.register(OW$PHASE, (mc, screen, w, h) -> {
                if (!(screen instanceof TitleScreen)) return;

                var prefs = SettingsPrefs.get();

                List<AbstractWidget> buttons = Screens.getButtons(screen);

                if (!prefs.accessibilityButton) {
                    for (Iterator<AbstractWidget> it = buttons.iterator(); it.hasNext();) {
                        AbstractWidget wgt = it.next();
                        if (wgt.getMessage().getString().toLowerCase().contains("access")) {
                            it.remove();

                            if (wgt instanceof SpriteIconButton ti) { ti.visible = false; ti.active = false; }
                        }
                    }
                }

                if (prefs.realmsButtons) return;

                final Component REALMS    = Component.translatable("menu.online");
                final Component COPYRIGHT = Component.translatable("title.credits");

                AbstractWidget realmsBtn = null;
                for (AbstractWidget wgt : buttons) {
                    if (wgt.getMessage().equals(REALMS)) { realmsBtn = wgt; break; }
                }

                if (realmsBtn == null) return;

                final int centerLeft  = (screen.width - 200) / 2;
                final int centerRight = centerLeft + 200;

                final int delta = realmsBtn.getHeight() + V_SPACING;
                final int cutY  = realmsBtn.getY();

                for (AbstractWidget wgt : buttons) {
                    if (wgt == realmsBtn || !wgt.visible) continue;
                    if (wgt.getMessage().equals(COPYRIGHT)) continue;

                    boolean inCenterColumn = (wgt.getX() <= centerRight) && (wgt.getX() + wgt.getWidth() >= centerLeft);
                    boolean isSmallIcon    = (wgt instanceof SpriteIconButton) && wgt.getWidth() <= 22 && wgt.getHeight() <= 22;

                    if (wgt.getY() >= cutY && (inCenterColumn || isSmallIcon)
                            && (wgt instanceof Button || wgt instanceof SpriteIconButton)) {
                        wgt.setY(wgt.getY() - delta);
                    }
                }
                buttons.remove(realmsBtn);
            });
        }
    }
}