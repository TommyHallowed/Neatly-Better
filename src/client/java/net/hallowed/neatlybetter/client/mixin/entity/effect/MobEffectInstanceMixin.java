package net.hallowed.neatlybetter.client.mixin.entity.effect;

import net.hallowed.neatlybetter.util.NTEffectInstance;

import net.minecraft.world.effect.MobEffectInstance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements NTEffectInstance {

    @Unique private int neatlybetter$maxDuration;
    @Shadow private int duration;

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;IIZZZLnet/minecraft/world/effect/MobEffectInstance;)V", at = @At("RETURN"))
    private void neatlybetter$onInit(CallbackInfo ci) {
        this.neatlybetter$maxDuration = this.duration;
    }

    @Inject(method = "setDetailsFrom", at = @At("RETURN"))
    private void neatlybetter$onCopyFrom(MobEffectInstance effect, CallbackInfo ci) {
        int incomingMax = ((NTEffectInstance) effect).neatlybetter$getMaxDuration();

        if (incomingMax > this.neatlybetter$maxDuration) {
            this.neatlybetter$maxDuration = incomingMax;
        }
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void neatlybetter$onUpdate(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        int incomingMax = ((NTEffectInstance) effect).neatlybetter$getMaxDuration();
        if (incomingMax > this.neatlybetter$maxDuration) {
            this.neatlybetter$maxDuration = incomingMax;
        }
    }

    @Override public int neatlybetter$getMaxDuration() { return this.neatlybetter$maxDuration; }
    @Override public void neatlybetter$setMaxDuration(int max) { this.neatlybetter$maxDuration = max; }
}