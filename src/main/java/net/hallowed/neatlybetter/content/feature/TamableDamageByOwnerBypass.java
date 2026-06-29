package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

public final class TamableDamageByOwnerBypass {
    private TamableDamageByOwnerBypass() {}

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(TamableDamageByOwnerBypass::onAllowDamage);
    }

    private static boolean onAllowDamage(LivingEntity entity,
                                         DamageSource source,
                                         float amount) {
        if (!NTServerConfig.CONFIG.tamableFriendlyFire.get()) return true;
        if (entity instanceof TamableAnimal animal && animal.isTame()) {
            Entity attacker = source.getEntity();
            if (attacker == null) return true;
            if (attacker.isShiftKeyDown()) return true;
            Entity owner = animal.getOwner();
            return owner == null || !attacker.getUUID().equals(owner.getUUID());
        }
        return true;
    }
}