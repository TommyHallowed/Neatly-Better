package net.hallowed.oldways.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void oldways$onSugarCaneRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (world == null) return;
        if (!world.isRaining()) return;
        if (!world.canSeeSky(pos.above())) return;

        SugarCaneBlock self = (SugarCaneBlock)(Object)this;

        for (int attempt = 0; attempt < 2; attempt++) {
            if (!world.isEmptyBlock(pos.above())) continue;

            int i = 1;
            while (world.getBlockState(pos.below(i)).is(self)) {
                i++;
            }

            if (i < 3) {
                int age = state.getValue(SugarCaneBlock.AGE);
                if (age == 15) {
                    world.setBlockAndUpdate(pos.above(), self.defaultBlockState());
                    world.setBlock(pos, state.setValue(SugarCaneBlock.AGE, 0), 260);
                } else {
                    world.setBlock(pos, state.setValue(SugarCaneBlock.AGE, age + 1), 260);
                }
            }
        }
    }
}
