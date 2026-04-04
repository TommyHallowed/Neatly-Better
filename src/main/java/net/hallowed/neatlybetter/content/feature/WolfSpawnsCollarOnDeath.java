package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.init.ModDataComponents;
import net.hallowed.neatlybetter.init.ModItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.TagValueOutput;

public final class WolfSpawnsCollarOnDeath {
    private WolfSpawnsCollarOnDeath() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(WolfSpawnsCollarOnDeath::onAfterDeath);
    }

    private static void onAfterDeath(net.minecraft.world.entity.LivingEntity entity,
                                     net.minecraft.world.damagesource.DamageSource damageSource) {
        if (!NTServerConfig.CONFIG.wolfImprovements.get()) return;
        if (!(entity instanceof Wolf wolf) || !wolf.isTame()) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        ItemStack collar = new ItemStack(ModItems.WOLF_COLLAR);

        TagValueOutput tagValueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        wolf.saveWithoutId(tagValueOutput);
        CompoundTag tag = tagValueOutput.buildResult();
        collar.set(ModDataComponents.WOLF_DATA, CustomData.of(tag));
        DyeColor color = wolf.getCollarColor();
        collar.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor()));

        if (wolf.hasCustomName()) {
            collar.set(DataComponents.ITEM_NAME,
                    Component.literal("")
                            .append(wolf.getCustomName())
                            .append("'s Collar"));
        }

        ItemEntity itemEntity = new ItemEntity(
                level, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), collar);
        itemEntity.setDefaultPickUpDelay();
        itemEntity.setUnlimitedLifetime();
        level.addFreshEntity(itemEntity);
    }
}