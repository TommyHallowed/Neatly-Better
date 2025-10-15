package net.hallowed.oldways.mixin.entity.generic;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(
            method = "handleShoulderEntities",
            at = @At("HEAD"),
            cancellable = true)
    private void oldways$cancelShortNoDamageJumpDrops(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (!self.inPowderSnow && !self.isTouchingWater() && !self.isSleeping() && !self.isGliding() && self.fallDistance <= self.getSafeFallDistance()) {
            ci.cancel();
        }
    }
}
