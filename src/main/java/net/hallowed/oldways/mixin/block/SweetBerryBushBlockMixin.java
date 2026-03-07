package net.hallowed.oldways.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void oldways$onSweetBerryRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (world == null) return;
        if (!world.isRaining()) return;
        if (!world.canSeeSky(pos.above())) return;

        SweetBerryBushBlock self = (SweetBerryBushBlock)(Object)this;

        for (int i = 0; i < 2; i++) {
            if (random.nextInt(5) == 0) {
                try {
                    self.performBonemeal(world, random, pos, state);
                } catch (Throwable ignored) {}
            }
        }
    }
}
