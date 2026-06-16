package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.NeatlyBetter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceGateBlock.class)
public abstract class FenceGateBlockMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FenceGateBlock;registerDefaultState" +
                            "(Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private void neatlybetter$wrapSetDefaultState(FenceGateBlock instance, BlockState blockState, Operation<Void> original) {
        original.call(instance, blockState.setValue(NeatlyBetter.GLUED, false));
    }

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    private void neatlybetter$injectGluedProperty(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(NeatlyBetter.GLUED);
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$blockToggleWhenGlued(BlockState state, Level level, BlockPos pos,
                                                   Player player, BlockHitResult hitResult,
                                                   CallbackInfoReturnable<InteractionResult> cir) {
        if (state.hasProperty(NeatlyBetter.GLUED) && state.getValue(NeatlyBetter.GLUED)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$blockRedstoneWhenGlued(BlockState state, Level level, BlockPos pos,
                                                     Block block, @Nullable Orientation orientation,
                                                     boolean movedByPiston, CallbackInfo ci) {
        if (state.hasProperty(NeatlyBetter.GLUED) && state.getValue(NeatlyBetter.GLUED)) {
            ci.cancel();
        }
    }
}