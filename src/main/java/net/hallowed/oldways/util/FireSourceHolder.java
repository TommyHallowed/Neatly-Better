package net.hallowed.oldways.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;

public interface FireSourceHolder {
    Block oldways$getLastFireSource();
    void oldways$setLastFireSource(Block block);

    static FireSourceHolder of(Entity e) {
        return (FireSourceHolder) e;
    }

    static Block normalize(Block b) {
        return (b == Blocks.SOUL_FIRE || b == Blocks.FIRE) ? b : Blocks.FIRE;
    }
}
