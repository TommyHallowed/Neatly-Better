package net.hallowed.neatlybetter.client.mixin.entity.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @WrapOperation(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;resetToggleKeys()V"
            )
    )
    private void neatlybetter$preserveSprintToggleOnRespawn(Operation<Void> original) {
        KeyMapping sprintKey = Minecraft.getInstance().options.keySprint;
        boolean wasSprintToggled = sprintKey.isDown();

        original.call();

        if (wasSprintToggled) {
            sprintKey.setDown(true);
        }
    }
}
