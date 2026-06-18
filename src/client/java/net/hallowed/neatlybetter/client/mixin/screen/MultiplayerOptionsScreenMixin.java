package net.hallowed.neatlybetter.client.mixin.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.MultiplayerOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerOptionsScreen.class)
public abstract class MultiplayerOptionsScreenMixin extends Screen {

    @Unique
    private @Nullable CycleButton<GameType> neatlybetter$gameModeButton = null;

    protected MultiplayerOptionsScreenMixin(Component title) {
        super(title);
    }

    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;create(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/CycleButton$OnValueChange;)Lnet/minecraft/client/gui/components/CycleButton;",
                    ordinal = 2
            )
    )
    private CycleButton<@NotNull Boolean> neatlybetter$hideAllowCommandsButton(CycleButton<@NotNull Boolean> button) {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null && !server.getWorldData().isAllowCommands()) {
            button.active = false;
            button.visible = false;
        }
        return button;
    }

    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;create(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/CycleButton$OnValueChange;)Lnet/minecraft/client/gui/components/CycleButton;",
                    ordinal = 1
            )
    )
    private CycleButton<@NotNull GameType> neatlybetter$captureGameModeButton(CycleButton<@NotNull GameType> button) {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null && !server.getWorldData().isAllowCommands()) {
            this.neatlybetter$gameModeButton = button;
        }
        return button;
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void neatlybetter$centerGameModeButton(CallbackInfo ci) {
        if (this.neatlybetter$gameModeButton != null) {
            this.neatlybetter$gameModeButton.setX(this.width / 2 - this.neatlybetter$gameModeButton.getWidth() / 2);
        }
    }
}