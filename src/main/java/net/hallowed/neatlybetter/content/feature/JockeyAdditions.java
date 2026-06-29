package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class JockeyAdditions {
    private JockeyAdditions() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            switch (entity) {
                case Stray stray when !stray.isBaby() && (stray.getVehicle() == null) -> {
                    if (stray.getRandom().nextInt(70) == 0) {
                        spawnPolarBearMount(level, stray);
                    }
                }
                case ZombifiedPiglin zpiglin when !zpiglin.isBaby() && (zpiglin.getVehicle() == null) -> {
                    if (zpiglin.getY() <= 120 && zpiglin.getRandom().nextInt(100) == 0) {
                        spawnZoglinMount(level, zpiglin);
                    }
                }
                default -> {}
            }
        });
    }

    private static void spawnPolarBearMount(ServerLevel level, Stray stray) {
        PolarBear bear = EntityTypes.POLAR_BEAR.create(level, EntitySpawnReason.JOCKEY);
        if (bear == null) return;

        setupMount(level, bear, stray);
        level.addFreshEntity(bear);
        stray.startRiding(bear, false, false);
    }

    private static void spawnZoglinMount(ServerLevel level, ZombifiedPiglin zpiglin) {
        Zoglin zoglin = EntityTypes.ZOGLIN.create(level, EntitySpawnReason.JOCKEY);
        if (zoglin == null) return;

        setupMount(level, zoglin, zpiglin);
        zpiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SPEAR));
        zpiglin.populateDefaultEquipmentEnchantments(
                level,
                level.getRandom(),
                level.getCurrentDifficultyAt(zpiglin.blockPosition())
        );
        level.addFreshEntity(zoglin);
        zpiglin.startRiding(zoglin, false, false);
    }

    private static void setupMount(ServerLevel level, Mob mount, Mob rider) {
        mount.snapTo(rider.getX(), rider.getY(), rider.getZ(), rider.getYRot(), 0.0F);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(rider.blockPosition());
        mount.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
    }
}