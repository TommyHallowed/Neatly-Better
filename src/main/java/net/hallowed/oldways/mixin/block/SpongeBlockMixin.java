package net.hallowed.oldways.mixin.block;

import net.minecraft.block.SpongeBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpongeBlock.class)
public abstract class SpongeBlockMixin {

    @Shadow @Final @Mutable public static int ABSORB_RADIUS;
    @Shadow @Final @Mutable public static int ABSORB_LIMIT;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void oldways$raiseAbsorbParams(CallbackInfo ci) {
        ABSORB_RADIUS = 10;
        ABSORB_LIMIT = 256;
    }

    @ModifyConstant(method = "absorbWater", constant = @Constant(intValue = 6))
    private int oldways$radiusInIterate(int original) {
        return 10;
    }

    @ModifyConstant(method = "absorbWater", constant = @Constant(intValue = 65))
    private int oldways$limitInIterate(int original) {
        return 257;
    }
}
