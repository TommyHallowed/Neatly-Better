package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WitherSkeletonArcher {
    private WitherSkeletonArcher() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!NTServerConfig.CONFIG.witherSkeletonArcher.get()) return;

            if (entity instanceof WitherSkeleton wsk
                    && !wsk.getMainHandItem().is(Items.BOW)) {

                int chance = switch (level.getDifficulty()) {
                    case HARD   -> 5;
                    case NORMAL -> 10;
                    default     -> 0;
                };

                if (chance > 0 && wsk.getRandom().nextInt(chance) == 0) {
                    makeArcher(level, wsk);
                }
            }
        });
    }

    private static void makeArcher(ServerLevel level, WitherSkeleton wsk) {
        wsk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        wsk.populateDefaultEquipmentEnchantments(
                level,
                level.getRandom(),
                level.getCurrentDifficultyAt(wsk.blockPosition())
        );
    }
}