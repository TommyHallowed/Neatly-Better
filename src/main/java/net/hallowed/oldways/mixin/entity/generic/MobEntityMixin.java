package net.hallowed.oldways.mixin.entity.generic;

import net.hallowed.oldways.content.feature.HostileAttributeTweaks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
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
            double extra = HostileAttributeTweaks.SPIDER_EXTRA_REACH;
            if (extra != 0.0) cir.setReturnValue(base.expand(extra, extra, extra));
        } else if (self instanceof IronGolemEntity) {
            double extra = HostileAttributeTweaks.IRON_GOLEM_EXTRA_REACH;
            if (extra != 0.0) cir.setReturnValue(base.expand(0.0, extra, 0.0));
        }
    }
}
