package net.hallowed.neatlybetter.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;

import net.hallowed.neatlybetter.client.config.NTClientConfig;

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

@Mixin(value = TitleScreen.class, priority = 400)
public abstract class TitleScreenMixin {

    @Shadow @Nullable private RealmsNotificationsScreen realmsNotificationsScreen;

    @Unique private static final int V_SPACING = 4;

    @Unique private static final Identifier neatlybetter$PHASE = Identifier.fromNamespaceAndPath("neatly-better", "title_buttons_late");
    @Unique private static boolean neatlybetter$afterInitHooked = false;

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            index = 1
    )
    private String neatlybetter$stripFabricModded(String original) {
        if (NTClientConfig.CONFIG.customBranding.get()) {
            return "Minecraft " + net.minecraft.SharedConstants.getCurrentVersion().name();
        }
        return original;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void neatlybetter$hookAfterInitAndKillNotifier(CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.realmsButtons.get()) {
            this.realmsNotificationsScreen = null;
        }

        if (!neatlybetter$afterInitHooked) {
            neatlybetter$afterInitHooked = true;
            ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, neatlybetter$PHASE);
            ScreenEvents.AFTER_INIT.register(neatlybetter$PHASE, (mc, screen, w, h) -> {
                if (!(screen instanceof TitleScreen)) return;

                List<AbstractWidget> buttons = Screens.getWidgets(screen);

                if (!NTClientConfig.CONFIG.accessibilityButton.get()) {
                    for (Iterator<AbstractWidget> it = buttons.iterator(); it.hasNext();) {
                        AbstractWidget wgt = it.next();
                        if (wgt.getMessage().getString().toLowerCase().contains("access")) {
                            it.remove();

                            if (wgt instanceof SpriteIconButton ti) { ti.visible = false; ti.active = false; }
                        }
                    }
                }

                if (NTClientConfig.CONFIG.realmsButtons.get()) return;

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