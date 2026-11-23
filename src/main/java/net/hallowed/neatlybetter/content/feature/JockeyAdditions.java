package net.hallowed.neatlybetter.content.feature;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Stray on a polar bear, zombified piglin on a zoglin. Called from the {@code finalizeSpawn} mixins
 * (AbstractSkeletonMixin / ZombieMixin), the same way vanilla spawns chicken jockeys, so it rolls once
 * per spawn instead of on every chunk load.
 */
public final class JockeyAdditions {
    private JockeyAdditions() {}

    public static void onFinalizeSpawn(Mob mob, ServerLevelAccessor level,
                                       DifficultyInstance difficulty, EntitySpawnReason reason) {
        if (reason == EntitySpawnReason.CONVERSION || reason == EntitySpawnReason.JOCKEY) return;
        if (mob.isBaby() || mob.getVehicle() != null) return;

        switch (mob) {
            case Stray stray when stray.getRandom().nextInt(70) == 0 -> {
                PolarBear bear = EntityTypes.POLAR_BEAR.create(level.getLevel(), EntitySpawnReason.JOCKEY);
                if (bear == null) return;
                mount(level, difficulty, bear, stray);
            }
            case ZombifiedPiglin zpiglin when zpiglin.getY() <= 120 && zpiglin.getRandom().nextInt(100) == 0 -> {
                Zoglin zoglin = EntityTypes.ZOGLIN.create(level.getLevel(), EntitySpawnReason.JOCKEY);
                if (zoglin == null) return;
                zpiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SPEAR));
                zpiglin.populateDefaultEquipmentEnchantments(level, zpiglin.getRandom(), difficulty);
                mount(level, difficulty, zoglin, zpiglin);
            }
            default -> {}
        }
    }

    private static void mount(ServerLevelAccessor level, DifficultyInstance difficulty, Mob mount, Mob rider) {
        mount.snapTo(rider.getX(), rider.getY(), rider.getZ(), rider.getYRot(), 0.0F);
        mount.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
        rider.startRiding(mount, false, false);
        level.addFreshEntity(mount);
    }
}
