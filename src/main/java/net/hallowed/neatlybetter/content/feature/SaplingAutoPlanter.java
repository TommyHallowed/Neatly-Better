package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;

public final class SaplingAutoPlanter {


    private SaplingAutoPlanter() {}

    public static void init() {
        ServerTickEvents.END_LEVEL_TICK.register(SaplingAutoPlanter::onWorldTick);
    }

    private static void onWorldTick(ServerLevel level) {
        if (NTServerConfig.CONFIG.saplingAutoReplant.isFalse()) return;

        java.util.List<ItemEntity> candidates = new java.util.ArrayList<>();

        level.getEntities(
                EntityTypeTest.forClass(ItemEntity.class),
                entity -> {
                    if (!entity.isRemoved() && entity.getAge() >= 5980) {
                        candidates.add(entity);
                    }
                    return AbortableIterationConsumer.Continuation.CONTINUE.shouldAbort();
                }
        );

        for (ItemEntity entity : candidates) {
            tryPlantSapling(level, entity);
        }
    }

    private static void tryPlantSapling(ServerLevel level, ItemEntity entity) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) return;

        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        if (!(blockItem.getBlock() instanceof SaplingBlock sapling)) return;

        BlockPos groundPos = entity.getOnPos();
        BlockPos plantPos  = groundPos.above();

        if (!level.isLoaded(plantPos)) return;

        BlockState saplingState = sapling.defaultBlockState();
        if (!saplingState.canSurvive(level, plantPos)) return;

        if (!level.getBlockState(plantPos).canBeReplaced()) return;

        level.setBlock(plantPos, saplingState, 3);
        entity.discard();
    }
}