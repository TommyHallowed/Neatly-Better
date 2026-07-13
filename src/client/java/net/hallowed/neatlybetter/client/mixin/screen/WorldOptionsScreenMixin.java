package net.hallowed.neatlybetter.client.mixin.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.WorldOptionsScreen;
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

@Mixin(WorldOptionsScreen.class)
public abstract class WorldOptionsScreenMixin extends Screen {

    @Unique private @Nullable CycleButton<GameType> neatlybetter$gameModeButton = null;
    @Unique private @Nullable CycleButton<Boolean> neatlybetter$allowCommandsButton = null;
    @Unique private @Nullable Button neatlybetter$restrictionsButton = null;
    @Unique private boolean neatlybetter$allowCommandsHidden = false;

    protected WorldOptionsScreenMixin(Component title) {
        super(title);
    }

    // ── worldOptionsHideAllowCommands ── (createGameModeButton runs before createAllowCommandsButton,
    // so this re-checks the condition independently rather than relying on the hidden flag below)
    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/options/WorldOptionsScreen;createGameModeButton(Lnet/minecraft/client/server/IntegratedServer;)Lnet/minecraft/client/gui/components/CycleButton;"
            )
    )
    private CycleButton<@NotNull GameType> neatlybetter$lockGameModeButton(CycleButton<@NotNull GameType> button) {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null && !server.getWorldData().isAllowCommands()) {
            button.active = false;
            this.neatlybetter$gameModeButton = button;
        }
        return button;
    }

    // ── worldOptionsHideAllowCommands ──
    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/options/WorldOptionsScreen;createAllowCommandsButton(Lnet/minecraft/client/server/IntegratedServer;)Lnet/minecraft/client/gui/components/CycleButton;"
            )
    )
    private CycleButton<@NotNull Boolean> neatlybetter$hideAllowCommandsButton(CycleButton<@NotNull Boolean> button) {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null && !server.getWorldData().isAllowCommands()) {
            button.active = false;
            button.visible = false;
            this.neatlybetter$allowCommandsButton = button;
            this.neatlybetter$allowCommandsHidden = true;
        }
        return button;
    }

    // ── worldOptionsHideAllowCommands (capture Restrictions so it can be moved) ──
    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/options/WorldOptionsScreen;createRestrictionsButton()Lnet/minecraft/client/gui/components/Button;"
            )
    )
    private Button neatlybetter$captureRestrictionsButton(Button button) {
        if (this.neatlybetter$allowCommandsHidden) {
            this.neatlybetter$restrictionsButton = button;
        }
        return button;
    }

    // ── worldOptionsHideAllowCommands (move Restrictions into the freed Allow Commands slot) ──
    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void neatlybetter$moveRestrictionsIntoAllowCommandsSlot(CallbackInfo ci) {
        if (this.neatlybetter$allowCommandsHidden
                && this.neatlybetter$allowCommandsButton != null
                && this.neatlybetter$restrictionsButton != null) {
            this.neatlybetter$restrictionsButton.setX(this.neatlybetter$allowCommandsButton.getX());
            this.neatlybetter$restrictionsButton.setY(this.neatlybetter$allowCommandsButton.getY());
        }
    }

    // ── worldOptionsHideAllowCommands (vanilla re-enables Game Mode here based purely on
    // hardcore/permission state, so the lock has to be reasserted after it runs) ──
    @Inject(method = "onGamemasterPermissionChanged", at = @At("TAIL"))
    private void neatlybetter$reapplyGameModeLock(boolean hasGamemasterPermission, CallbackInfo ci) {
        if (this.neatlybetter$allowCommandsHidden && this.neatlybetter$gameModeButton != null) {
            this.neatlybetter$gameModeButton.active = false;
        }
    }
}