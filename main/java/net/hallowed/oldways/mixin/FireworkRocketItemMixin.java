package net.hallowed.oldways.mixin;

import net.hallowed.oldways.config.CommonConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
