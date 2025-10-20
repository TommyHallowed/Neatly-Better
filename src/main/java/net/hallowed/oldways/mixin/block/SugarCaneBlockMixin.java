package net.hallowed.oldways.mixin.block;

import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void oldways$onSugarCaneRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (world == null) return;
        if (!world.isRaining()) return;
        if (!world.isSkyVisible(pos.up())) return;

        SugarCaneBlock self = (SugarCaneBlock)(Object)this;

        for (int attempt = 0; attempt < 2; attempt++) {
            if (!world.isAir(pos.up())) continue;

            int i = 1;
            while (world.getBlockState(pos.down(i)).isOf(self)) {
                i++;
            }

            if (i < 3) {
                int age = state.get(SugarCaneBlock.AGE);
                if (age == 15) {
                    world.setBlockState(pos.up(), self.getDefaultState());
                    world.setBlockState(pos, state.with(SugarCaneBlock.AGE, 0), 260);
                } else {
                    world.setBlockState(pos, state.with(SugarCaneBlock.AGE, age + 1), 260);
                }
            }
        }
    }
}
