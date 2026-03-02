package net.hallowed.oldways.mixin.entity.generic;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    @Inject(method = "getAttackBox", at = @At("RETURN"), cancellable = true)
    private void oldways$extraReach(CallbackInfoReturnable<Box> cir) {
        Object self = this;
        Box base = cir.getReturnValue();

        if (self instanceof SpiderEntity) {
                cir.setReturnValue(base.expand(0.65, 0.65, 0.65));
        }
    }
}
