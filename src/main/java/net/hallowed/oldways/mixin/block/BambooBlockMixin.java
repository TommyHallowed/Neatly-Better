package net.hallowed.oldways.mixin.block;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooBlock.class)
public class BambooBlockMixin {

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void oldways$onBambooRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (world == null) return;
        if (!world.isRaining()) return;
        if (!world.isSkyVisible(pos.up())) return;

        BambooBlock self = (BambooBlock)(Object)this;

        for (int i = 0; i < 1; i++) {
            if (random.nextInt(3) == 0) {
                try {
                    self.grow(world, random, pos, state);
                } catch (Throwable ignored) {}
            }
        }
    }
}
