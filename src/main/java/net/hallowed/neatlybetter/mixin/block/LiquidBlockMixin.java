package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.util.CopperGrateHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {

    /**
     * Vanilla water only schedules its bubble-column tick when magma or soul sand is directly below.
     * Also do it for a waterlogged grate; the tick then resolves the real source under the grate stack.
     * Covers onPlace, updateShape and neighborChanged, which all go through this method.
     */
    @Inject(method = "tryScheduleBubbleBlockColumn", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$scheduleThroughGrate(ScheduledTickAccess ticks, BlockPos pos, BlockState stateBelow, CallbackInfo ci) {
        if (CopperGrateHelper.isWaterloggedCopperGrate(stateBelow)) {
            ticks.scheduleTick(pos, (LiquidBlock) (Object) this, 20);
            ci.cancel();
        }
    }
}
