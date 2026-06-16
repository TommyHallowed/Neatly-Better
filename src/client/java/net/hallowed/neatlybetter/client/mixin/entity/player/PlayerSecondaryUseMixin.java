package net.hallowed.neatlybetter.client.mixin.entity.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.hallowed.neatlybetter.client.util.ClickThroughState;
import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Player.class)
public class PlayerSecondaryUseMixin {

    @Inject(method = "isSecondaryUseActive", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$noCancelWhenDyeingSign(CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof LocalPlayer) {
            if (!NTCommonConfig.CONFIG.clickThrough.get()) return;
            if (ClickThroughState.isDyeOnSign) {
                cir.setReturnValue(false);
                cir.cancel();
                ClickThroughState.isDyeOnSign = false;
            }
        }
    }
}