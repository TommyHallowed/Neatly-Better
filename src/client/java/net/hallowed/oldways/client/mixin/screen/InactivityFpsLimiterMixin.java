package net.hallowed.oldways.client.mixin.screen;

import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InactivityFpsLimiter.class)
public abstract class InactivityFpsLimiterMixin {
    @Shadow private int maxFps;
    @Shadow public abstract InactivityFpsLimiter.LimitReason getLimitReason();

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void oldways$menuFpsFromGameOption(CallbackInfoReturnable<Integer> cir) {
        if (this.getLimitReason() == InactivityFpsLimiter.LimitReason.OUT_OF_LEVEL_MENU) {
            cir.setReturnValue(this.maxFps);
        }
    }
}
