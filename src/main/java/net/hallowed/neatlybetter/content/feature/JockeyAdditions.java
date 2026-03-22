package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class JockeyAdditions {
    private JockeyAdditions() {}

    private static final String PROCESSED_TAG = "neatly_better.jockey_checked";

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity.getTags().contains(PROCESSED_TAG)) return;
            entity.addTag(PROCESSED_TAG);

            switch (entity) {
                case Hoglin hoglin when !hoglin.isBaby() -> {
                    if (hoglin.getRandom().nextInt(100) == 0) {
                        spawnPiglinJockey(level, hoglin);
                    }
                }
                case PolarBear bear when !bear.isBaby() -> {
                    if (bear.getRandom().nextInt(100) == 0) {
                        spawnStrayJockey(level, bear);
                    }
                }
                case Zoglin zoglin when !zoglin.isBaby() -> {
                    if (zoglin.getY() <= 120 && zoglin.getRandom().nextInt(100) == 0) {
                        spawnZombifiedPiglinJockey(level, zoglin);
                    }
                }
                default -> {
                }
            }
        });
    }

    private static void spawnPiglinJockey(ServerLevel level, Hoglin hoglin) {
        Piglin piglin = EntityType.PIGLIN.create(level, EntitySpawnReason.JOCKEY);
        if (piglin == null) return;

        setupRider(level, piglin, hoglin);
        piglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SPEAR));
        piglin.populateDefaultEquipmentEnchantments(
                level,
                level.getRandom(),
                level.getCurrentDifficultyAt(hoglin.blockPosition())
        );
        level.addFreshEntity(piglin);
        piglin.startRiding(hoglin, false, false);
    }

    private static void spawnStrayJockey(ServerLevel level, PolarBear bear) {
        Stray stray = EntityType.STRAY.create(level, EntitySpawnReason.JOCKEY);
        if (stray == null) return;

        setupRider(level, stray, bear);
        level.addFreshEntity(stray);
        stray.startRiding(bear, false, false);
    }

    private static void spawnZombifiedPiglinJockey(ServerLevel level, Zoglin zoglin) {
        ZombifiedPiglin zpiglin = EntityType.ZOMBIFIED_PIGLIN.create(level, EntitySpawnReason.JOCKEY);
        if (zpiglin == null) return;

        setupRider(level, zpiglin, zoglin);
        zpiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SPEAR));
        zpiglin.populateDefaultEquipmentEnchantments(
                level,
                level.getRandom(),
                level.getCurrentDifficultyAt(zoglin.blockPosition())
        );
        level.addFreshEntity(zpiglin);
        zpiglin.startRiding(zoglin, false, false);
    }

    private static void setupRider(ServerLevel level, Mob rider, Mob mount) {
        rider.snapTo(mount.getX(), mount.getY(), mount.getZ(), mount.getYRot(), 0.0F);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(mount.blockPosition());
        rider.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
    }
}
