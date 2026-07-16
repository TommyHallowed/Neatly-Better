package net.hallowed.neatlybetter.mixin.item;

import net.hallowed.neatlybetter.config.NTServerConfig;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$disableFireworkBoosting(Level level, Player player, InteractionHand hand,
                                                      CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.isFallFlying()) return;
        if (NTServerConfig.CONFIG.doElytraFireworkBoosting.isTrue()) return;
        cir.setReturnValue(InteractionResult.FAIL);
    }

    @ModifyExpressionValue(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isFallFlying()Z"
            )
    )
    private boolean neatlybetter$allowGroundPlacementWhileFlying(boolean original) {
        return original && NTServerConfig.CONFIG.doElytraFireworkBoosting.isTrue();
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;dropAllLeashConnections(Lnet/minecraft/world/entity/player/Player;)Z"
            )
    )
    private boolean neatlybetter$preventLeadBreaking(Player instance, @Nullable Player player) {
        return false;
    }
}