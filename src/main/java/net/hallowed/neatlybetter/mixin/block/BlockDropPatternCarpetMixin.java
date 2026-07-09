package net.hallowed.neatlybetter.mixin.block;

import com.mojang.logging.LogUtils;

import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.data.ChunkCarpetPatterns;
import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WoolCarpetBlock;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockDropPatternCarpetMixin {

    @Unique
    private static final Logger neatlybetter$LOGGER = LogUtils.getLogger();

    @Inject(
            method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD")
    )
    private static void neatlybetter$stampCarpetPatternOnDrop(
            Level level, BlockPos pos, ItemStack itemStack, CallbackInfo ci
    ) {
        if (level.isClientSide()) {
            return;
        }

        if (!(itemStack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof WoolCarpetBlock)) {
            return;
        }

        ChunkCarpetPatterns patterns = level.getChunkAt(pos).getAttached(ModData.CARPET_PATTERN_DATA);
        if (patterns == null) {
            return;
        }

        CarpetPatternData data = patterns.consumePendingDrop(pos);
        if (data == null) {
            data = patterns.get(pos);
        }
        if (data == null || data.isEmpty()) {
            return;
        }

        itemStack.set(ModData.CARPET_PATTERNS, data);
        neatlybetter$LOGGER.debug("[NeatlyBetter/CarpetPatterns] Stamped pattern onto dropped carpet from {}", pos);
    }
}