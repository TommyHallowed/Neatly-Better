package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    @Inject(method = "randomTick", at = @At("RETURN"))
    private void neatlybetter$onCropRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {

        if (!NTServerConfig.CONFIG.rainIncreasesCropGrowth.get()) return;
        if (!world.isRainingAt(pos.above())) return;

        CropBlock self = (CropBlock)(Object)this;
        int age = self.getAge(state);
        int max = self.getMaxAge();
        if (age >= max) return;

        for (int i = 0; i < 2; i++) {
            if (random.nextInt(25) == 0) {
                int newAge = Math.min(max, self.getAge(world.getBlockState(pos)) + 1);
                world.setBlock(pos, self.getStateForAge(newAge), 2);
                if (newAge >= max) break;
            }
        }
    }
}
