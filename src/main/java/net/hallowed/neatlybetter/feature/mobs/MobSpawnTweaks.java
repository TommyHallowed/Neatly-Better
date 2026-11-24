package net.hallowed.neatlybetter.feature.mobs;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Spawn-time rolls: wither skeleton archers and extra jockeys (stray on a polar bear, zombified piglin on a
 * zoglin). Called from the {@code finalizeSpawn} mixins (AbstractSkeletonMixin / ZombieMixin), the same way
 * vanilla spawns chicken jockeys, so each roll happens once per spawn instead of on every chunk load.
 */
public final class MobSpawnTweaks {
    private MobSpawnTweaks() {}

    public static void onFinalizeSpawn(Mob mob, ServerLevelAccessor level,
                                       DifficultyInstance difficulty, EntitySpawnReason reason) {
        if (mob instanceof WitherSkeleton wsk) witherSkeletonArcher(wsk, level, difficulty, reason);
        jockeys(mob, level, difficulty, reason);
    }

    /** Some wither skeletons get a bow (after vanilla equipment, so it replaces the sword). */
    private static void witherSkeletonArcher(WitherSkeleton wsk, ServerLevelAccessor level,
                                             DifficultyInstance difficulty, EntitySpawnReason reason) {
        if (!NTServerConfig.CONFIG.witherSkeletonArcher.get()) return;
        if (reason == EntitySpawnReason.CONVERSION) return;
        if (wsk.getMainHandItem().is(Items.BOW)) return;

        int chance = switch (level.getDifficulty()) {
            case HARD   -> 5;
            case NORMAL -> 10;
            default     -> 0;
        };
        if (chance <= 0 || wsk.getRandom().nextInt(chance) != 0) return;

        wsk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        wsk.populateDefaultEquipmentEnchantments(level, wsk.getRandom(), difficulty);
        wsk.reassessWeaponGoal();
    }

    private static void jockeys(Mob mob, ServerLevelAccessor level,
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
