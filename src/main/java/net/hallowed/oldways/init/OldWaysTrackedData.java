package net.hallowed.oldways.init;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public final class OldWaysTrackedData {
    public static final TrackedData<Byte> OLDWAYS_SOUL_FIRE = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BYTE);

    private OldWaysTrackedData() {}
}
