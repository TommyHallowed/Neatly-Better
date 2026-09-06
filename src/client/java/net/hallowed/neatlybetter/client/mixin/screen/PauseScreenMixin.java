package net.hallowed.neatlybetter.client.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;

import net.hallowed.neatlybetter.api.NTCompat;
import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.screen.ScreenshotManagerScreen;
import net.hallowed.neatlybetter.client.util.ModTextures;

import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.SmallModMenuButtonWidget;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FriendsButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonLinks;

import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) { super(title); }

    @Unique private static final int V_SPACING = 4;
    @Unique private static final int ROW_HEIGHT = 20;
    @Unique private static final int BUTTON_WIDTH_FULL = 204;
    @Unique private static final int BUTTON_WIDTH_HALF = 98;
    @Unique private static final int HALF_ROW_SPACING = BUTTON_WIDTH_FULL - (BUTTON_WIDTH_HALF * 2);

    @Unique
    private static final Component SCREENSHOTS_MESSAGE = Component.translatable("neatlybetter.pause_menu.screenshots");

    @Unique private static final Component RETURN_TO_GAME     = Component.translatable("menu.returnToGame");
    @Unique private static final Component ADVANCEMENTS       = Component.translatable("gui.advancements");
    @Unique private static final Component STATS              = Component.translatable("gui.stats");
    @Unique private static final Component SEND_FEEDBACK      = Component.translatable("menu.sendFeedback");
    @Unique private static final Component REPORT_BUGS        = Component.translatable("menu.reportBugs");
    @Unique private static final Component OPTIONS            = Component.translatable("menu.options");
    @Unique private static final Component MULTIPLAYER_OPTIONS = Component.translatable("menu.multiplayerOptions.button");
    @Unique private static final Component PLAYER_REPORTING   = Component.translatable("menu.playerReporting");

    @Unique private static final Identifier neatlybetter$PHASE = Identifier.fromNamespaceAndPath("neatly-better", "pause_buttons_late");
    @Unique private static boolean neatlybetter$afterInitHooked = false;

    @Inject(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/SpriteIconButton;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;Z)Lnet/minecraft/client/gui/components/SpriteIconButton$Builder;",
                    ordinal = 0
            )
    )
    private void neatlybetter$addScreenshotsButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
        if (NTClientConfig.CONFIG.screenshotsPauseButton.isFalse()) return;

        SpriteIconButton screenshotsButton = SpriteIconButton.builder(SCREENSHOTS_MESSAGE, _ -> this.minecraft.gui.setScreen(new ScreenshotManagerScreen(this)), true)
                .width(20)
                .sprite(ModTextures.SCREENSHOTS_ICON, 15, 15)
                .withTootip()
                .build();

        iconButtonRow.addChild(screenshotsButton);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void neatlybetter$hookAfterInit(CallbackInfo ci) {
        if (neatlybetter$afterInitHooked) return;
        neatlybetter$afterInitHooked = true;

        ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, neatlybetter$PHASE);
        ScreenEvents.AFTER_INIT.register(neatlybetter$PHASE, (_, screen, _, _) -> {
            if (!(screen instanceof PauseScreen pauseScreen) || !pauseScreen.showsPauseMenu()) return;

            List<AbstractWidget> widgets = Screens.getWidgets(screen);

            AbstractWidget returnBtn = null, advancementsBtn = null, statsBtn = null,
                    reportBugsBtn = null, feedbackBtn = null, friendsBtn = null,
                    reportingBtn = null, optionsBtn = null, multiplayerOptionsBtn = null,
                    screenshotsBtn = null, modsBtn = null, modsFullBtn = null, disconnectBtn = null;

            for (AbstractWidget wgt : widgets) {
                if (wgt instanceof FriendsButton) {
                    friendsBtn = wgt;
                    continue;
                }
                if (neatlybetter$isModsIconButton(wgt)) {
                    modsBtn = wgt;
                    continue;
                }
                if (neatlybetter$isModsFullButton(wgt)) {
                    modsFullBtn = wgt;
                    continue;
                }

                Component msg = wgt.getMessage();
                if (neatlybetter$keyEquals(msg, RETURN_TO_GAME)) {
                    returnBtn = wgt;
                } else if (neatlybetter$keyEquals(msg, ADVANCEMENTS)) {
                    advancementsBtn = wgt;
                } else if (neatlybetter$keyEquals(msg, STATS)) {
                    statsBtn = wgt;
                } else if (wgt instanceof SpriteIconButton && neatlybetter$keyEquals(msg, REPORT_BUGS)) {
                    reportBugsBtn = wgt;
                } else if (wgt instanceof SpriteIconButton && neatlybetter$keyEquals(msg, SEND_FEEDBACK)) {
                    feedbackBtn = wgt;
                } else if (wgt instanceof SpriteIconButton && neatlybetter$keyEquals(msg, PLAYER_REPORTING)) {
                    reportingBtn = wgt;
                } else if (wgt instanceof SpriteIconButton && neatlybetter$keyEquals(msg, SCREENSHOTS_MESSAGE)) {
                    screenshotsBtn = wgt;
                } else if (neatlybetter$keyEquals(msg, MULTIPLAYER_OPTIONS)) {
                    multiplayerOptionsBtn = wgt;
                } else if (neatlybetter$keyEquals(msg, OPTIONS)) {
                    optionsBtn = wgt;
                }
            }

            for (AbstractWidget wgt : widgets) {
                if (wgt instanceof Button
                        && wgt != returnBtn && wgt != advancementsBtn && wgt != statsBtn
                        && wgt != optionsBtn && wgt != multiplayerOptionsBtn && wgt != modsBtn && wgt != modsFullBtn) {
                    disconnectBtn = wgt;
                }
            }

            if (reportBugsBtn != null && NTClientConfig.CONFIG.reportBugsButton.isFalse()) {
                widgets.remove(reportBugsBtn);
                reportBugsBtn.visible = false;
                reportBugsBtn.active = false;
                reportBugsBtn = null;
            }
            if (feedbackBtn != null && NTClientConfig.CONFIG.feedbackButton.isFalse()) {
                widgets.remove(feedbackBtn);
                feedbackBtn.visible = false;
                feedbackBtn.active = false;
                feedbackBtn = null;
            }
            if (friendsBtn != null && NTClientConfig.CONFIG.friendsPauseButton.isFalse()) {
                widgets.remove(friendsBtn);
                friendsBtn.visible = false;
                friendsBtn.active = false;
                friendsBtn = null;
            }
            if (reportingBtn != null && NTClientConfig.CONFIG.reportButton.isFalse()) {
                widgets.remove(reportingBtn);
                reportingBtn.visible = false;
                reportingBtn.active = false;
                reportingBtn = null;
            }

            if (NTClientConfig.CONFIG.legacyPauseMenuLayout.isTrue()) {
                neatlybetter$applyLegacyLayout(screen, widgets, returnBtn, advancementsBtn, statsBtn,
                        reportBugsBtn, feedbackBtn, friendsBtn, reportingBtn,
                        optionsBtn, multiplayerOptionsBtn, screenshotsBtn, modsBtn, modsFullBtn, disconnectBtn);
            } else {
                List<AbstractWidget> present = new ArrayList<>();
                for (AbstractWidget w : new AbstractWidget[]{ screenshotsBtn, reportBugsBtn, feedbackBtn, friendsBtn, reportingBtn }) {
                    if (w != null) present.add(w);
                }
                if (!present.isEmpty()) {
                    int rowY = present.getFirst().getY();
                    if (modsBtn != null) present.add(modsBtn);
                    neatlybetter$centerRow(screen, rowY, present.toArray(new AbstractWidget[0]));
                } else if (modsBtn != null) {
                    neatlybetter$centerRow(screen, modsBtn.getY(), modsBtn);
                }
            }
        });
    }

    @Unique
    private static void neatlybetter$applyLegacyLayout(
            Screen screen,
            List<AbstractWidget> widgets,
            @Nullable AbstractWidget returnBtn,
            @Nullable AbstractWidget advancementsBtn,
            @Nullable AbstractWidget statsBtn,
            @Nullable AbstractWidget reportBugsBtn,
            @Nullable AbstractWidget feedbackBtn,
            @Nullable AbstractWidget friendsBtn,
            @Nullable AbstractWidget reportingBtn,
            @Nullable AbstractWidget optionsBtn,
            @Nullable AbstractWidget multiplayerOptionsBtn,
            @Nullable AbstractWidget screenshotsBtn,
            @Nullable AbstractWidget modsBtn,
            @Nullable AbstractWidget modsFullBtn,
            @Nullable AbstractWidget disconnectBtn
    ) {
        if (returnBtn == null || optionsBtn == null || disconnectBtn == null) return;

        int y = returnBtn.getY();

        neatlybetter$centerRow(screen, y, returnBtn);
        y += ROW_HEIGHT + V_SPACING;

        List<AbstractWidget> topRow = new ArrayList<>();
        if (advancementsBtn != null) topRow.add(advancementsBtn);
        if (statsBtn != null) topRow.add(statsBtn);
        if (!topRow.isEmpty()) {
            neatlybetter$centerRow(screen, y, HALF_ROW_SPACING, topRow.toArray(new AbstractWidget[0]));
            y += ROW_HEIGHT + V_SPACING;
        }

        int fbCount = (feedbackBtn != null ? 1 : 0) + (reportBugsBtn != null ? 1 : 0);
        int fbWidth = fbCount == 2 ? BUTTON_WIDTH_HALF : BUTTON_WIDTH_FULL;

        AbstractWidget feedbackGrid = feedbackBtn != null
                ? neatlybetter$replaceWithGridButton(widgets, feedbackBtn, neatlybetter$buildFeedbackGridButton(screen, fbWidth, feedbackBtn.active))
                : null;
        AbstractWidget reportBugsGrid = reportBugsBtn != null
                ? neatlybetter$replaceWithGridButton(widgets, reportBugsBtn, neatlybetter$buildReportBugsGridButton(screen, fbWidth, reportBugsBtn.active))
                : null;

        List<AbstractWidget> feedbackCore = new ArrayList<>();
        if (feedbackGrid != null) feedbackCore.add(feedbackGrid);
        if (reportBugsGrid != null) feedbackCore.add(reportBugsGrid);

        boolean hasModsFullRow = modsFullBtn != null;

        if (!feedbackCore.isEmpty()) {
            if (hasModsFullRow) {
                neatlybetter$centerRow(screen, y, HALF_ROW_SPACING, feedbackCore.toArray(new AbstractWidget[0]));
            } else {
                neatlybetter$centerRowWithFlankingIcons(screen, y, reportingBtn,
                        feedbackCore.toArray(new AbstractWidget[0]), screenshotsBtn, HALF_ROW_SPACING);
            }
            y += ROW_HEIGHT + V_SPACING;
        } else if (!hasModsFullRow && (reportingBtn != null || screenshotsBtn != null)) {
            neatlybetter$centerRow(screen, y, reportingBtn, screenshotsBtn);
            y += ROW_HEIGHT + V_SPACING;
        }

        if (hasModsFullRow) {
            neatlybetter$centerRowWithFlankingIcons(screen, y, reportingBtn,
                    new AbstractWidget[]{ modsFullBtn }, screenshotsBtn, 0);
            y += ROW_HEIGHT + V_SPACING;
        }

        List<AbstractWidget> optionsCore = new ArrayList<>();
        optionsCore.add(optionsBtn);
        if (multiplayerOptionsBtn != null) optionsCore.add(multiplayerOptionsBtn);
        neatlybetter$centerRowWithFlankingIcons(screen, y, friendsBtn,
                optionsCore.toArray(new AbstractWidget[0]), modsBtn, HALF_ROW_SPACING);
        y += ROW_HEIGHT + V_SPACING;

        neatlybetter$centerRow(screen, y, disconnectBtn);
    }

    @Unique
    private static Button neatlybetter$buildReportBugsGridButton(Screen screen, int width, boolean active) {
        Button btn = Button.builder(REPORT_BUGS, ConfirmLinkScreen.confirmLink(screen, CommonLinks.SNAPSHOT_BUGS_FEEDBACK))
                .width(width)
                .build();
        btn.active = active;
        return btn;
    }

    @Unique
    private static Button neatlybetter$buildFeedbackGridButton(Screen screen, int width, boolean active) {
        Button btn = Button.builder(SEND_FEEDBACK, ConfirmLinkScreen.confirmLink(screen,
                        SharedConstants.getCurrentVersion().stable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK))
                .width(width)
                .build();
        btn.active = active;
        return btn;
    }

    @Unique
    private static boolean neatlybetter$isModsIconButton(AbstractWidget widget) {
        return NTCompat.MODMENU && widget instanceof SmallModMenuButtonWidget;
    }

    @Unique
    private static boolean neatlybetter$isModsFullButton(AbstractWidget widget) {
        return NTCompat.MODMENU && widget instanceof ModMenuButtonWidget;
    }

    @Unique
    private static boolean neatlybetter$keyEquals(Component msg, Component expected) {
        return neatlybetter$translationKey(msg).equals(neatlybetter$translationKey(expected));
    }

    @Unique
    private static String neatlybetter$translationKey(Component c) {
        return c.getContents() instanceof TranslatableContents tc ? tc.getKey() : c.getString();
    }

    @Unique
    private static AbstractWidget neatlybetter$replaceWithGridButton(List<AbstractWidget> widgets, AbstractWidget oldWidget, AbstractWidget newWidget) {
        int idx = widgets.indexOf(oldWidget);
        widgets.remove(oldWidget);
        oldWidget.visible = false;
        oldWidget.active = false;

        if (idx >= 0 && idx <= widgets.size()) {
            widgets.add(idx, newWidget);
        } else {
            widgets.add(newWidget);
        }
        return newWidget;
    }

    @Unique
    private static void neatlybetter$centerRow(Screen screen, int y, AbstractWidget... maybeWidgets) {
        neatlybetter$centerRow(screen, y, V_SPACING, maybeWidgets);
    }

    @Unique
    private static void neatlybetter$centerRowWithFlankingIcons(
            Screen screen, int y,
            @Nullable AbstractWidget leftIcon,
            AbstractWidget[] core,
            @Nullable AbstractWidget rightIcon,
            int coreGap
    ) {
        int coreWidth = 0;
        for (AbstractWidget w : core) coreWidth += w.getWidth();
        if (core.length > 1) coreWidth += coreGap * (core.length - 1);

        int coreX = screen.width / 2 - coreWidth / 2;

        int x = coreX;
        for (AbstractWidget w : core) {
            w.setX(x);
            w.setY(y);
            x += w.getWidth() + coreGap;
        }

        if (leftIcon != null) {
            leftIcon.setX(coreX - V_SPACING - leftIcon.getWidth());
            leftIcon.setY(y);
        }
        if (rightIcon != null) {
            rightIcon.setX(coreX + coreWidth + V_SPACING);
            rightIcon.setY(y);
        }
    }

    @Unique
    private static void neatlybetter$centerRow(Screen screen, int y, int spacing, AbstractWidget... maybeWidgets) {
        List<AbstractWidget> row = new ArrayList<>();
        for (AbstractWidget w : maybeWidgets) {
            if (w != null) row.add(w);
        }
        if (row.isEmpty()) return;

        int totalWidth = (row.size() - 1) * spacing;
        for (AbstractWidget w : row) totalWidth += w.getWidth();

        int x = screen.width / 2 - totalWidth / 2;
        for (AbstractWidget w : row) {
            w.setX(x);
            w.setY(y);
            x += w.getWidth() + spacing;
        }
    }
}