package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.NeatlyBetter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VineBlock.class)
public abstract class VineBlockMixin {
    @WrapOperation(
            method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/VineBlock;registerDefaultState" +
                    "(Lnet/minecraft/world/level/block/state/BlockState;)V"
    )
    )
    private void wrapSetDefaultState(VineBlock instance, BlockState blockState, Operation<Void> original) {
        original.call(instance, blockState.setValue(NeatlyBetter.SHEARED, false));
    }

    @Inject(method = "createBlockStateDefinition", at = @At(value = "TAIL"))
    private void appendProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo info) {
        builder.add(NeatlyBetter.SHEARED);
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), cancellable = true)
    private void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo info
    ) {
        if (state.getValue(NeatlyBetter.SHEARED)) {
            info.cancel();
        }
    }
}