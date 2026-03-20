package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;

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
    private void neatlybetter$onSweetBerryRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {

        if (!NTServerConfig.CONFIG.rainIncreasesCropGrowth.get()) return;
        if (!world.isRainingAt(pos.above())) return;

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
