package net.hallowed.neatlybetter.mixin.level;

import net.hallowed.neatlybetter.data.ChunkCarpetPatterns;
import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Inject(
            method = "setBlockState",
            at = @At("RETURN")
    )
    private void neatlybetter$clearCarpetPatternOnChange(
            BlockPos pos,
            BlockState state,
            int flags,
            CallbackInfoReturnable<BlockState> cir
    ) {
        BlockState oldState = cir.getReturnValue();
        if (oldState == null) {
            return;
        }

        if (!(oldState.getBlock() instanceof WoolCarpetBlock) || oldState.is(state.getBlock())) {
            return;
        }

        LevelChunk self = (LevelChunk) (Object) this;
        ChunkCarpetPatterns patterns = self.getAttached(ModData.CARPET_PATTERN_DATA);
        if (patterns == null || !patterns.has(pos)) {
            return;
        }

        ChunkCarpetPatterns updated = patterns.withRemove(pos);
        self.setAttached(ModData.CARPET_PATTERN_DATA, updated);
    }
}