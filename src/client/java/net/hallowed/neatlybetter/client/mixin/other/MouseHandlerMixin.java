package net.hallowed.neatlybetter.client.mixin.other;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.neatlybetter.client.feature.zoom.Zoom;
import net.hallowed.neatlybetter.client.util.PortalInventoryGuard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"))
    private void neatlybetter$markInputActive(CallbackInfo ci) {
        PortalInventoryGuard.isInputActive = true;
    }

    @Inject(method = "onButton", at = @At("RETURN"))
    private void neatlybetter$clearInputActive(CallbackInfo ci) {
        PortalInventoryGuard.isInputActive = false;
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$zoomScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (handle == 0L || handle != this.minecraft.getWindow().handle()
                || this.minecraft.gui.screen() != null
                || this.minecraft.gui.overlay() != null
                || this.minecraft.player == null) {
            return;
        }

        if (Zoom.handleScroll(yoffset)) {
            this.minecraft.getFramerateLimitTracker().onInputReceived();
            ci.cancel();
        }
    }

    @WrapOperation(method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void neatlybetter$scaleZoomSensitivity(LocalPlayer player, double xo, double yo, Operation<Void> original) {
        double scale = Zoom.sensitivityScale();
        original.call(player, xo * scale, yo * scale);
    }
}