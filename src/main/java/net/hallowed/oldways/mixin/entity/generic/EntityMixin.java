package net.hallowed.oldways.mixin.entity.generic;

import net.hallowed.oldways.content.entity.vehicle.LavaBoatEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /* ------------------------ lava-boat behavior ------------------------ */

    @Inject(method = "isInLava()Z", at = @At("HEAD"), cancellable = true)
    private void oldways$ignoreLavaWhileOnWarpedBoat(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        if (self.hasVehicle() && self.getVehicle() instanceof LavaBoatEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setOnFireFromLava", at = @At("HEAD"), cancellable = true)
    private void oldways$noLavaIgniteWhileOnWarpedBoat(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self.hasVehicle() && self.getVehicle() instanceof LavaBoatEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "igniteByLava", at = @At("HEAD"), cancellable = true)
    private void oldways$dontIgniteWhenOnWarpedBoat(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self.getVehicle() instanceof LavaBoatEntity) {
            ci.cancel();
        }
    }
}
