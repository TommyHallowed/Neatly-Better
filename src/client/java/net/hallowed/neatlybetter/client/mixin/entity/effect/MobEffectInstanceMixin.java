package net.hallowed.neatlybetter.client.mixin.entity.effect;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
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
        boolean effectBars = true;
        try {
            effectBars = NTClientConfig.CONFIG.effectBars.get();
        } catch (IllegalStateException ignored) {
        }
        if (!effectBars) return;
        this.neatlybetter$maxDuration = this.duration;
    }

    @Inject(method = "setDetailsFrom", at = @At("RETURN"))
    private void neatlybetter$onCopyFrom(MobEffectInstance copy, CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.effectBars.get()) return;
        int incomingMax = ((NTEffectInstance) copy).neatlybetter$getMaxDuration();

        if (incomingMax > this.neatlybetter$maxDuration) {
            this.neatlybetter$maxDuration = incomingMax;
        }
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void neatlybetter$onUpdate(MobEffectInstance takeOver, CallbackInfoReturnable<Boolean> cir) {
        if (!NTClientConfig.CONFIG.effectBars.get()) return;
        int incomingMax = ((NTEffectInstance) takeOver).neatlybetter$getMaxDuration();
        if (incomingMax > this.neatlybetter$maxDuration) {
            this.neatlybetter$maxDuration = incomingMax;
        }
    }

    @Override public int neatlybetter$getMaxDuration() { return this.neatlybetter$maxDuration; }
    @Override public void neatlybetter$setMaxDuration(int max) { this.neatlybetter$maxDuration = max; }
}