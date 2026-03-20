package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooStalkBlock.class)
public class BambooStalkBlockMixin {

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void neatlybetter$onBambooRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {

        if (!NTServerConfig.CONFIG.rainIncreasesCropGrowth.get()) return;
        if (!world.isRainingAt(pos.above())) return;

        BambooStalkBlock self = (BambooStalkBlock)(Object)this;

        if (random.nextInt(3) == 0) {
            try {
                self.performBonemeal(world, random, pos, state);
            } catch (Throwable ignored) {}
        }
    }
}
