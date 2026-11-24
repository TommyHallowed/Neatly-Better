package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.util.CopperGrateHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** All copper grates are WaterloggedTransparentBlock. */
@Mixin(WaterloggedTransparentBlock.class)
public abstract class CopperGrateBlockMixin {

    /**
     * Something under a waterlogged grate changed (source placed or removed by any means: player, piston,
     * explosion, command, another grate). Re-check the bubble column above the whole grate stack.
     */
    @Inject(method = "updateShape", at = @At("HEAD"))
    private void neatlybetter$recheckColumnAbove(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                                 BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos,
                                                 BlockState neighbourState, RandomSource random,
                                                 CallbackInfoReturnable<BlockState> cir) {
        if (directionToNeighbour == Direction.DOWN && CopperGrateHelper.isWaterloggedCopperGrate(state)) {
            CopperGrateHelper.scheduleColumnAboveStack(level, ticks, pos);
        }
    }
}
