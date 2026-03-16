package net.hallowed.neatlybetter.mixin.entity.player;

import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(
            method = "handleShoulderEntities",
            at = @At("HEAD"),
            cancellable = true)
    private void neatlybetter$cancelShortNoDamageJumpDrops(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!self.isInPowderSnow && !self.isInWater() && !self.isSleeping() && !self.isFallFlying() && self.fallDistance <= self.getMaxFallDistance()) {
            ci.cancel();
        }
    }
}
