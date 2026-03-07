package net.hallowed.oldways.mixin.item;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void oldways$onlyDragonBurstBoosts(Level world, Player user, InteractionHand hand,
                                               CallbackInfoReturnable<InteractionResult> cir) {
        if (!(user.level() instanceof ServerLevel sw)) return;
        if (sw.getGameRules().get(ModGameRules.DO_ELYTRA_FIREWORK_BOOSTING)) return;
        if (!user.isFallFlying()) return;
        cir.setReturnValue(InteractionResult.PASS);
    }
}
