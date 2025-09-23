package net.hallowed.mixin;

import net.hallowed.config.CommonConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    /** If boosting is disabled: block AIR-use while gliding (no boost / no consume). */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void hallowed$maybeBlockAirUseWhileGliding(World world, PlayerEntity user, Hand hand,
                                                       CallbackInfoReturnable<ActionResult> cir) {
        if (user != null && user.isGliding() && !CommonConfigManager.elytraBoostingEnabled()) {
            // PASS => "not handled": nothing happens on air use; item not consumed.
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    /**
     * If boosting is disabled: ensure block-use places rockets while gliding.
     * We spoof isGliding() to false only when boosting is disabled.
     */
    @Redirect(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isGliding()Z"
            )
    )
    private boolean hallowed$maybeAllowPlacementWhileGliding(PlayerEntity player) {
        if (!CommonConfigManager.elytraBoostingEnabled()) {
            return false; // pretend not gliding → placement path
        }
        return player.isGliding(); // vanilla when boosting is enabled
    }
}
