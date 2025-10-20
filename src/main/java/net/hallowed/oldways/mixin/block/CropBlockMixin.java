package net.hallowed.oldways.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    @Inject(method = "randomTick", at = @At("RETURN"))
    private void oldways$onCropRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (world == null) return;
        if (!world.isRaining()) return;
        if (!world.isSkyVisible(pos.up())) return;

        CropBlock self = (CropBlock)(Object)this;
        int age = self.getAge(state);
        int max = self.getMaxAge();
        if (age >= max) return;

        for (int i = 0; i < 2; i++) {
            int chance = 25;
            if (random.nextInt(chance) == 0) {
                int newAge = Math.min(max, self.getAge(world.getBlockState(pos)) + 1);
                world.setBlockState(pos, self.withAge(newAge), 2);
                if (newAge >= max) break;
            }
        }
    }
}
