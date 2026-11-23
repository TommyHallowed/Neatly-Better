package net.hallowed.neatlybetter.content.feature;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Gives some wither skeletons a bow. Called from {@code AbstractSkeletonMixin} at the end of
 * {@code finalizeSpawn}, so it rolls once per spawn and never again when the chunk is reloaded.
 */
public final class WitherSkeletonArcher {
    private WitherSkeletonArcher() {}

    public static void onFinalizeSpawn(WitherSkeleton wsk, ServerLevelAccessor level,
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
}
