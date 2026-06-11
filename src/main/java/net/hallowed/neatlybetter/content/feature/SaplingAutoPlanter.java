package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;

/**
 * SaplingAutoPlanter
 *
 * When a sapling item entity (vanilla or modded) is about to expire
 * and it is resting above a block that allows the sapling to be planted,
 * the item is consumed and the sapling is placed instead of despawning.
 *
 * Uses only Fabric's ServerTickEvents — no mixins required.
 */
public final class SaplingAutoPlanter {

    // Fire well before the 6000-tick vanilla despawn to guarantee we always run first.
    private static final int PLANT_THRESHOLD = 5980;

    private SaplingAutoPlanter() {}

    public static void init() {
        ServerTickEvents.END_LEVEL_TICK.register(SaplingAutoPlanter::onWorldTick);
    }

    private static void onWorldTick(ServerLevel level) {
        // getEntities with EntityTypeTest iterates over all tracked entities of a given
        // type without requiring an AABB, returning early via the AbortableIterationConsumer.
        // We collect into a list first so discarding inside the loop is safe.
        java.util.List<ItemEntity> candidates = new java.util.ArrayList<>();

        level.getEntities(
                EntityTypeTest.forClass(ItemEntity.class),
                entity -> {
                    if (!entity.isRemoved() && entity.getAge() >= PLANT_THRESHOLD) {
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

        // Only process BlockItems whose block is a SaplingBlock.
        // Covers all modded saplings that follow the standard convention
        // of extending SaplingBlock — no registry tag lookups needed.
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        if (!(blockItem.getBlock() instanceof SaplingBlock sapling)) return;

        // getOnPos() returns the solid block the entity is standing ON (e.g. grass).
        // The sapling must be placed one block ABOVE that (the air slot at entity level).
        BlockPos groundPos = entity.getOnPos();   // the supporting block, e.g. grass
        BlockPos plantPos  = groundPos.above();   // the slot the sapling occupies

        // Never touch unloaded chunks.
        if (!level.isLoaded(plantPos)) return;

        // canSurvive(level, plantPos) checks level.getBlockState(plantPos.below()),
        // which is groundPos — exactly the soil validation we need.
        BlockState saplingState = sapling.defaultBlockState();
        if (!saplingState.canSurvive(level, plantPos)) return;

        // The planting slot must be air or replaceable (handles short grass, flowers, etc.).
        if (!level.getBlockState(plantPos).canBeReplaced()) return;

        // All checks passed: place the sapling and remove the item entity.
        level.setBlock(plantPos, saplingState, 3);
        entity.discard();
    }
}