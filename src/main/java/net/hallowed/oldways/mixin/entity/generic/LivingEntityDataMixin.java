package net.hallowed.oldways.mixin.entity.generic;

import net.hallowed.oldways.init.OldWaysTrackedData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityDataMixin {

    @Inject(method = "initDataTracker(Lnet/minecraft/entity/data/DataTracker$Builder;)V", at = @At("TAIL"))
    private void oldways$initDataTracker(CallbackInfo ci) {
        try {
            LivingEntity self = (LivingEntity)(Object)this;
            DataTracker tracker = self.getDataTracker();
            // Use reflection to call startTracking with the shared tracked data key from OldWaysTrackedData
            try {
                var m = tracker.getClass().getMethod("startTracking", TrackedData.class, Object.class);
                m.invoke(tracker, OldWaysTrackedData.OLDWAYS_SOUL_FIRE, (byte)0);
            } catch (NoSuchMethodException nsme) {
                try {
                    var m2 = tracker.getClass().getMethod("startTracking", TrackedData.class, byte.class);
                    m2.invoke(tracker, OldWaysTrackedData.OLDWAYS_SOUL_FIRE, (byte)0);
                } catch (Throwable ignored) {}
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }
}
