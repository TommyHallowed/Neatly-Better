package net.hallowed.oldways.mixin.entity.boat;

import net.hallowed.oldways.content.entity.vehicle.LavaBoatEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class AbstractBoatEntityMixin {
    @Redirect(method = "checkBoatInWater", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean oldways$allowLavaInWaterCheck(FluidState state, TagKey<Fluid> tag) {
        AbstractBoatEntity self = (AbstractBoatEntity)(Object)this;
        if (tag == FluidTags.WATER && self instanceof LavaBoatEntity) {
            return state.isIn(FluidTags.WATER) || state.isIn(FluidTags.LAVA);
        }
        return state.isIn(tag);
    }

    @Redirect(method = "getUnderWaterLocation", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean oldways$allowLavaUnder(FluidState state, TagKey<Fluid> tag) {
        AbstractBoatEntity self = (AbstractBoatEntity)(Object)this;
        if (tag == FluidTags.WATER && self instanceof LavaBoatEntity) {
            return state.isIn(FluidTags.WATER) || state.isIn(FluidTags.LAVA);
        }
        return state.isIn(tag);
    }

    @Inject(method = "updatePaddles", at = @At("TAIL"))
    private void oldways$slowWarpedBoatGlide(CallbackInfo ci) {
        AbstractBoatEntity self = (AbstractBoatEntity)(Object)this;
        if (self instanceof LavaBoatEntity && self.isInLava()) {
            self.setVelocity(self.getVelocity().multiply(0.65, 1.0, 0.65));
        }
    }
}
