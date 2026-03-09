package net.hallowed.oldways.client.mixin.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShareToLanScreen.class)
public abstract class ShareToLanScreenMixin extends Screen {

    protected ShareToLanScreenMixin(Component title) {
        super(title);
    }

    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;create(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/CycleButton$OnValueChange;)Lnet/minecraft/client/gui/components/CycleButton;",
                    ordinal = 0
            )
    )
    private CycleButton<@NotNull GameType> oldways$centerGameModeButtonIfLocked(CycleButton<@NotNull GameType> button) {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null && !server.getWorldData().isAllowCommands()) {
            button.setX(this.width / 2 - 75);
        }
        return button;
    }

    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;create(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/CycleButton$OnValueChange;)Lnet/minecraft/client/gui/components/CycleButton;",
                    ordinal = 1
            )
    )
    private CycleButton<@NotNull Boolean> oldways$hideCheatButtonIfLocked(CycleButton<@NotNull Boolean> button) {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null && !server.getWorldData().isAllowCommands()) {
            button.active = false;
            button.visible = false;
        }
        return button;
    }
}