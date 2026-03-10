package net.hallowed.oldways.client.mixin.options;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public abstract class GameOptionsMixin {

    // ln(100) ≈ 4.605 — maps [0,1] linear → [0,1] exponential
    // At 50% slider → ~9% actual volume (matches human perception)
    @Unique private static final double K  = Math.log(100.0);
    @Unique private static final double EK = Math.expm1(K);   // e^K − 1 = 99

    @Unique
    private static float logGain(float v) {
        if (v <= 0f) return 0f;
        if (v >= 1f) return 1f;
        return (float) (Math.expm1(K * v) / EK);
    }

    @ModifyReturnValue(
            method = "getSoundSourceVolume",
            at = @At("RETURN")
    )
    private float oldways$logVolume(float original) {
        return logGain(original);
    }
}