package net.hallowed.oldways.mixin.entity.general;

import net.hallowed.oldways.content.entity.vehicle.LavaBoatEntity;
import net.hallowed.oldways.util.EntityInsideFireHandler;
import net.hallowed.oldways.util.FireSourceHolder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements FireSourceHolder {

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

    @Unique private Block oldways$lastFireSource = Blocks.FIRE;

    @Inject(method = "setOnFireForTicks", at = @At("HEAD"))
    private void oldways$captureSourceOnIgnite(int ticks, CallbackInfo ci) {
        if (ticks > 0) {
            EntityInsideFireHandler.setLastFireSourceFromBlocks((Entity)(Object)this);
        }
    }

    @Override public Block oldways$getLastFireSource() { return oldways$lastFireSource; }
    @Override public void oldways$setLastFireSource(Block block) { this.oldways$lastFireSource = FireSourceHolder.normalize(block); }
}
