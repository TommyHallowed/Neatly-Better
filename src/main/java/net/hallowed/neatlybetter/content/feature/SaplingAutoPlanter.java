package net.hallowed.neatlybetter.content.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Plants a sapling item where it lies just before it despawns. Driven by ItemEntityMixin. */
public final class SaplingAutoPlanter {
    private SaplingAutoPlanter() {}

    public static void tryPlantSapling(ServerLevel level, ItemEntity entity) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty() || entity.isRemoved()) return;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        if (!(blockItem.getBlock() instanceof SaplingBlock sapling)) return;

        BlockPos plantPos = entity.getOnPos().above();
        if (!level.isLoaded(plantPos)) return;
        if (!level.getBlockState(plantPos).canBeReplaced()) return;

        BlockState saplingState = sapling.defaultBlockState();
        if (!saplingState.canSurvive(level, plantPos)) return;

        level.setBlock(plantPos, saplingState, 3);
        entity.discard();
    }
}
