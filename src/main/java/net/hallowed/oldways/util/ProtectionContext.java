package net.hallowed.oldways.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class ProtectionContext {
    private static final ThreadLocal<DamageSource> SRC = new ThreadLocal<>();
    private static final ThreadLocal<LivingEntity> ENT = new ThreadLocal<>();

    public static void set(LivingEntity ent, DamageSource src) {
        ENT.set(ent);
        SRC.set(src);
    }

    public static void clear() {
        ENT.remove();
        SRC.remove();
    }

    public static DamageSource src() { return SRC.get(); }
    public static LivingEntity ent() { return ENT.get(); }

    private ProtectionContext() {}
}
