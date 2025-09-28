package net.hallowed.oldways.api;

import net.hallowed.oldways.mixin.accessor.BlockEntityTypeBlocksAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Adds supported blocks to existing BlockEntityTypes (e.g., beds/banners). */
public final class OWBlockEntityTypeSupport {
    private OWBlockEntityTypeSupport() {}

    public static void addSupported(BlockEntityType<?> type, Block... more) {
        BlockEntityTypeBlocksAccessor acc = (BlockEntityTypeBlocksAccessor) type;
        Set<Block> set = new HashSet<>(acc.oldways$getBlocks());
        set.addAll(Arrays.asList(more));
        acc.oldways$setBlocks(set);
    }
}
