package net.hallowed.oldways.client.mixin.options;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class GameOptionsMixin {

    @Shadow public abstract OptionInstance<@NotNull Double> getSoundSourceOptionInstance(SoundSource category);

    @Unique private static final double K  = Math.log(100.0);
    @Unique private static final double EK = Math.expm1(K);

    @Unique
    private static float logGain(double v) {
        if (v <= 0.0) return 0f;
        if (v >= 1.0) return 1f;
        return (float)(Math.expm1(K * v) / EK);
    }

    @Inject(
            method = "getSoundSourceVolume",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$logVolume(SoundSource category, CallbackInfoReturnable<Float> cir) {
        double linear = this.getSoundSourceOptionInstance(category).get();
        cir.setReturnValue(logGain(linear));
    }
}
