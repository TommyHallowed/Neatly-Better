package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.client.screen.ScreenshotManagerScreen;
import net.hallowed.neatlybetter.client.util.ModTextures;

import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) { super(title); }

    @Unique
    private static final Component SCREENSHOTS_MESSAGE = Component.translatable("neatlybetter.pause_menu.screenshots");

    @Inject(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/SpriteIconButton;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;Z)Lnet/minecraft/client/gui/components/SpriteIconButton$Builder;",
                    ordinal = 0
            )
    )
    private void neatlybetter$addScreenshotsButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
        SpriteIconButton screenshotsButton = SpriteIconButton.builder(SCREENSHOTS_MESSAGE, _ -> this.minecraft.gui.setScreen(new ScreenshotManagerScreen(this)), true)
                .width(20)
                .sprite(ModTextures.SCREENSHOTS_ICON, 15, 15)
                .withTootip()
                .build();

        iconButtonRow.addChild(screenshotsButton);
    }
}