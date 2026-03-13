package net.hallowed.oldways.client.mixin.entity.player;

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

    /**
     * Preserves the sprint toggle state across respawns.

     * Vanilla calls KeyMapping.resetToggleKeys() in respawn(), which resets
     * ALL toggle key states — including sprint. This wraps that call to
     * save and restore the sprint key state, so players don't have to
     * re-toggle sprint every time they die.

     * Uses @WrapOperation for mod compatibility — chains with any other
     * mod wrapping the same call.
     */
    @WrapOperation(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;resetToggleKeys()V"
            )
    )
    private void oldways$preserveSprintToggleOnRespawn(Operation<Void> original) {
        // Capture sprint toggle state before reset
        KeyMapping sprintKey = Minecraft.getInstance().options.keySprint;
        boolean wasSprintToggled = sprintKey.isDown();

        // Let vanilla reset all toggle keys normally
        original.call();

        // Restore sprint toggle if it was active
        if (wasSprintToggled) {
            sprintKey.setDown(true);
        }
    }
}
