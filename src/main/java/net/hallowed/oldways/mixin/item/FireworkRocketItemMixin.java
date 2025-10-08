package net.hallowed.oldways.mixin.item;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void hallowed$maybeBlockAirUseWhileGliding(World world, PlayerEntity user, Hand hand,
                                                       CallbackInfoReturnable<ActionResult> cir) {
        if (user != null && user.isGliding()) {
            if (!(world instanceof ServerWorld sw)) return;
            if (sw.getGameRules().getBoolean(ModGameRules.ELYTRA_FIREWORK_BOOSTING)) return;
            cir.setReturnValue(ActionResult.FAIL);
            user.stopUsingItem();
        }
    }
}
