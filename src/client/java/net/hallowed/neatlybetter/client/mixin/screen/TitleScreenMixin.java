package net.hallowed.neatlybetter.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.screen.ScreenshotManagerScreen;
import net.hallowed.neatlybetter.client.util.ModTextures;

import com.terraformersmc.modmenu.config.ModMenuConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FriendsButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(value = TitleScreen.class, priority = 400)
public abstract class TitleScreenMixin {

    @Shadow @Nullable private RealmsNotificationsScreen realmsNotificationsScreen;
    @Shadow @Nullable private FriendsButton friends;

    @Unique private static final int V_SPACING = 4;

    @Unique private static final Component SCREENSHOTS_MESSAGE = Component.translatable("neatlybetter.pause_menu.screenshots");

    @Unique private static final Identifier neatlybetter$PHASE = Identifier.fromNamespaceAndPath("neatly-better", "title_buttons_late");
    @Unique private static boolean neatlybetter$afterInitHooked = false;

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            index = 1
    )
    private String neatlybetter$stripFabricModded(String original) {
        if (NTClientConfig.CONFIG.customBranding.isTrue()) {
            return "Minecraft " + net.minecraft.SharedConstants.getCurrentVersion().name();
        }
        return original;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void neatlybetter$hookAfterInitAndKillNotifier(CallbackInfo ci) {
        if (NTClientConfig.CONFIG.realmsButton.isFalse()) {
            this.realmsNotificationsScreen = null;
        }

        if (NTClientConfig.CONFIG.friendsButton.isFalse()) {
            this.friends = null;
        }

        if (!neatlybetter$afterInitHooked) {
            neatlybetter$afterInitHooked = true;
            ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, neatlybetter$PHASE);
            ScreenEvents.AFTER_INIT.register(neatlybetter$PHASE, (_, screen, _, _) -> {
                if (!(screen instanceof TitleScreen)) return;

                List<AbstractWidget> buttons = Screens.getWidgets(screen);

                if (NTClientConfig.CONFIG.accessibilityButton.isFalse()) {
                    neatlybetter$removeAccessibilityButton(buttons);
                }

                if (NTClientConfig.CONFIG.languageButton.isFalse()) {
                    neatlybetter$removeLanguageButton(buttons);
                }

                if (NTClientConfig.CONFIG.realmsButton.isFalse()) {
                    neatlybetter$removeRealmsButton(screen, buttons);
                }

                if (NTClientConfig.CONFIG.friendsButton.isFalse()) {
                    neatlybetter$removeFriendsButton(buttons);
                }

                AbstractWidget screenshotsButton = NTClientConfig.CONFIG.screenshotsButton.isFalse()
                        ? null
                        : neatlybetter$addScreenshotsButton(screen, buttons);

                if (NTClientConfig.CONFIG.legacyTitleScreenLayout.isTrue()) {
                    neatlybetter$applyLegacyLayout(screen, buttons, screenshotsButton);
                } else if (screenshotsButton != null) {
                    neatlybetter$positionScreenshotsButtonDefault(screen, buttons, screenshotsButton);
                } else {
                    neatlybetter$centerLoneModMenuIconDefault(screen, buttons);
                }
            });
        }
    }

    @Unique
    private static boolean neatlybetter$isModMenuIconStyle() {
        if (!FabricLoader.getInstance().isModLoaded("modmenu")) return false;
        return neatlybetter$modMenuStyleIsIcon();
    }

    @Unique
    private static boolean neatlybetter$modMenuStyleIsIcon() {
        return ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON;
    }

    @Unique
    private static void neatlybetter$removeAccessibilityButton(List<AbstractWidget> buttons) {
        for (Iterator<AbstractWidget> it = buttons.iterator(); it.hasNext();) {
            AbstractWidget wgt = it.next();
            if (wgt.getMessage().getString().toLowerCase().contains("access")) {
                it.remove();

                if (wgt instanceof SpriteIconButton ti) { ti.visible = false; ti.active = false; }
            }
        }
    }

    @Unique
    private static void neatlybetter$removeLanguageButton(List<AbstractWidget> buttons) {
        for (Iterator<AbstractWidget> it = buttons.iterator(); it.hasNext();) {
            AbstractWidget wgt = it.next();
            if (wgt.getMessage().getString().toLowerCase().contains("language")) {
                it.remove();

                if (wgt instanceof SpriteIconButton ti) { ti.visible = false; ti.active = false; }
            }
        }
    }

    @Unique
    private static void neatlybetter$removeFriendsButton(List<AbstractWidget> buttons) {
        for (Iterator<AbstractWidget> it = buttons.iterator(); it.hasNext();) {
            AbstractWidget wgt = it.next();
            if (wgt.getMessage().getString().toLowerCase().contains("friend")) {
                it.remove();

                if (wgt instanceof SpriteIconButton ti) { ti.visible = false; ti.active = false; }
            }
        }
    }

    @Unique
    private static void neatlybetter$removeRealmsButton(Screen screen, List<AbstractWidget> buttons) {
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
    }

    @Unique
    private static AbstractWidget neatlybetter$addScreenshotsButton(Screen screen, List<AbstractWidget> buttons) {
        SpriteIconButton screenshotsButton = SpriteIconButton.builder(SCREENSHOTS_MESSAGE, _ -> Minecraft.getInstance().gui.setScreen(new ScreenshotManagerScreen(screen)), true)
                .width(20)
                .sprite(ModTextures.SCREENSHOTS_ICON, 15, 15)
                .withTootip()
                .build();

        buttons.add(screenshotsButton);
        return screenshotsButton;
    }

    @Unique
    private static void neatlybetter$positionScreenshotsButtonDefault(Screen screen, List<AbstractWidget> buttons, AbstractWidget screenshotsButton) {
        final Component OPTIONS = Component.translatable("menu.options");

        AbstractWidget friendsBtn = null;
        AbstractWidget languageBtn = null;
        AbstractWidget accessibilityBtn = null;
        AbstractWidget optionsBtn = null;
        AbstractWidget modsBtn = null;

        for (AbstractWidget wgt : buttons) {
            if (wgt == screenshotsButton || !wgt.visible) continue;

            if (wgt.getMessage().equals(OPTIONS)) {
                optionsBtn = wgt;
                continue;
            }

            String msg = wgt.getMessage().getString().toLowerCase();
            if (msg.contains("friend")) {
                friendsBtn = wgt;
            } else if (msg.contains("language")) {
                languageBtn = wgt;
            } else if (msg.contains("access")) {
                accessibilityBtn = wgt;
            } else if (msg.contains("mods")) {
                modsBtn = wgt;
            }
        }

        final int rowY = friendsBtn != null ? friendsBtn.getY()
                : languageBtn != null ? languageBtn.getY()
                  : accessibilityBtn != null ? accessibilityBtn.getY()
                    : optionsBtn != null ? optionsBtn.getY() - 24
                      : screenshotsButton.getY();

        List<AbstractWidget> row = new ArrayList<>();
        row.add(screenshotsButton);
        if (friendsBtn != null) row.add(friendsBtn);
        if (languageBtn != null) row.add(languageBtn);
        if (accessibilityBtn != null) row.add(accessibilityBtn);
        if (modsBtn != null && neatlybetter$isModMenuIconStyle()) row.add(modsBtn);

        int totalWidth = (row.size() - 1) * V_SPACING;
        for (AbstractWidget wgt : row) {
            totalWidth += wgt.getWidth();
        }

        int x = screen.width / 2 - totalWidth / 2;
        for (AbstractWidget wgt : row) {
            wgt.setX(x);
            wgt.setY(rowY);
            x += wgt.getWidth() + V_SPACING;
        }
    }

    @Unique
    private static void neatlybetter$centerLoneModMenuIconDefault(Screen screen, List<AbstractWidget> buttons) {
        if (!neatlybetter$isModMenuIconStyle()) return;

        AbstractWidget modsBtn = null;
        for (AbstractWidget wgt : buttons) {
            if (!wgt.visible) continue;

            String message = wgt.getMessage().getString().toLowerCase();
            if (message.contains("mods")) {
                modsBtn = wgt;
            } else if (message.contains("friend") || message.contains("language") || message.contains("access")) {
                return;
            }
        }

        if (modsBtn != null) {
            modsBtn.setX(screen.width / 2 - modsBtn.getWidth() / 2);
        }
    }

    @Unique
    private static void neatlybetter$applyLegacyLayout(Screen screen, List<AbstractWidget> buttons, @Nullable AbstractWidget screenshotsButton) {
        final Component OPTIONS = Component.translatable("menu.options");
        final Component QUIT    = Component.translatable("menu.quit");

        AbstractWidget languageBtn = null;
        AbstractWidget accessibilityBtn = null;
        AbstractWidget friendsBtn = null;
        AbstractWidget optionsBtn = null;
        AbstractWidget quitBtn = null;
        AbstractWidget modsBtn = null;

        for (AbstractWidget wgt : buttons) {
            if (!wgt.visible) continue;

            Component message = wgt.getMessage();

            if (message.equals(OPTIONS)) {
                optionsBtn = wgt;
                continue;
            }
            if (message.equals(QUIT)) {
                quitBtn = wgt;
                continue;
            }

            String msg = message.getString().toLowerCase();
            if (msg.contains("language")) {
                languageBtn = wgt;
            } else if (msg.contains("access")) {
                accessibilityBtn = wgt;
            } else if (msg.contains("friend")) {
                friendsBtn = wgt;
            } else if (msg.contains("mods")) {
                modsBtn = wgt;
            }
        }

        if (optionsBtn == null || quitBtn == null) return;

        final int iconHeight = languageBtn != null ? languageBtn.getHeight()
                : accessibilityBtn != null ? accessibilityBtn.getHeight()
                  : friendsBtn != null ? friendsBtn.getHeight()
                    : 20;
        final int rowDelta = iconHeight + V_SPACING;

        final int anchorY = languageBtn != null ? languageBtn.getY()
                : accessibilityBtn != null ? accessibilityBtn.getY()
                  : friendsBtn != null ? friendsBtn.getY()
                    : optionsBtn.getY() - rowDelta;

        final int rowY = anchorY + 12;

        if (languageBtn != null) {
            languageBtn.setX(screen.width / 2 - 124);
            languageBtn.setY(rowY);
        }

        if (accessibilityBtn != null) {
            accessibilityBtn.setX(screen.width / 2 + 104);
            accessibilityBtn.setY(rowY);
        }

        optionsBtn.setY(rowY);
        quitBtn.setY(rowY);

        if (friendsBtn != null) {
            friendsBtn.setX(screen.width / 2 - 124);
            if (languageBtn != null) {
                friendsBtn.setY(rowY - rowDelta - 12);
            } else {
                friendsBtn.setY(rowY);
            }
        }

        final boolean modsIconStyle = modsBtn != null && neatlybetter$isModMenuIconStyle();
        final int columnX = screen.width / 2 + 104;

        final boolean modsTakesAccessibilitySlot = modsIconStyle && accessibilityBtn == null;

        final boolean screenshotsTakesAccessibilitySlot =
                screenshotsButton != null && accessibilityBtn == null && !modsTakesAccessibilitySlot;

        if (modsTakesAccessibilitySlot) {
            modsBtn.setX(columnX);
            modsBtn.setY(rowY);
        }

        if (screenshotsButton != null) {
            screenshotsButton.setX(columnX);
            screenshotsButton.setY(screenshotsTakesAccessibilitySlot ? rowY : (rowY - rowDelta - 12));
        }

        if (modsIconStyle && !modsTakesAccessibilitySlot) {
            int modsY = screenshotsButton != null
                    ? (rowY - rowDelta - 12) - (rowDelta)
                    : (rowY - rowDelta - 12);
            modsBtn.setX(columnX);
            modsBtn.setY(modsY);
        }
    }
}