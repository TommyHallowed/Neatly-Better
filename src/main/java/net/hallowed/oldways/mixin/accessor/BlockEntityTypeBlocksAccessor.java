package net.hallowed.oldways.mixin.accessor;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeBlocksAccessor {
    // In your Yarn this field is named "blocks". If your IDE complains, change
    // both accessors to "supportedBlocks" instead.
    @Accessor("blocks") Set<Block> oldways$getBlocks();
    @Accessor("blocks") void oldways$setBlocks(Set<Block> value);
}
