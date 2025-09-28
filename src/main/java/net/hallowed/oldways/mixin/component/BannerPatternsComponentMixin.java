package net.hallowed.oldways.mixin.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.component.type.BannerPatternsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerPatternsComponent.class)
public abstract class BannerPatternsComponentMixin {

    @Shadow @Final @Mutable
    public static Codec<BannerPatternsComponent> CODEC;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void oldways$relaxBannerLayerLimit(CallbackInfo ci) {
        final int MAX = 16;
        CODEC = CODEC.validate(comp -> {
            int size = comp.layers().size();
            return size <= MAX
                    ? DataResult.success(comp)
                    : DataResult.error(() -> "Too many banner layers: " + size + " (max " + MAX + ")");
        });
    }
}
