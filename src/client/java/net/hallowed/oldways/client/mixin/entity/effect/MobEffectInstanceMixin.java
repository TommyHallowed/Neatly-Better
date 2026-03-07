package net.hallowed.oldways.client.mixin.entity.effect;

import net.hallowed.oldways.util.OWEffectInstance;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements OWEffectInstance {

    @Unique private int oldways$maxDuration;
    @Shadow private int duration;

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;IIZZZLnet/minecraft/world/effect/MobEffectInstance;)V", at = @At("RETURN"))
    private void oldways$onInit(CallbackInfo ci) {
        this.oldways$maxDuration = this.duration;
    }

    @Inject(method = "setDetailsFrom", at = @At("RETURN"))
    private void oldways$onCopyFrom(MobEffectInstance effect, CallbackInfo ci) {
        // Safe to cast because on the client, all MobEffectInstances will have this interface implemented
        int incomingMax = ((OWEffectInstance) effect).oldways$getMaxDuration();

        // BUG FIX: Only adopt the incoming max if it is larger, preventing sync packets from shrinking it!
        if (incomingMax > this.oldways$maxDuration) {
            this.oldways$maxDuration = incomingMax;
        }
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void oldways$onUpdate(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        int incomingMax = ((OWEffectInstance) effect).oldways$getMaxDuration();
        if (incomingMax > this.oldways$maxDuration) {
            this.oldways$maxDuration = incomingMax;
        }
    }

    @Override public int oldways$getMaxDuration() { return this.oldways$maxDuration; }
    @Override public void oldways$setMaxDuration(int max) { this.oldways$maxDuration = max; }
}