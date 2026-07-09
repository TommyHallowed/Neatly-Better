package net.hallowed.neatlybetter.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.NeatlyBetter;
import net.hallowed.neatlybetter.api.NTCompat;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void neatlybetter$syncDoubleDoors(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (NTCompat.DOUBLEDOORS || player.isShiftKeyDown() || NTServerConfig.CONFIG.doubleDoorOpening.isFalse()) return;

        if (!cir.getReturnValue().consumesAction()) return;

        BlockState newState = level.getBlockState(pos);
        if (!(newState.getBlock() instanceof DoorBlock)) return;

        boolean isNowOpen = newState.getValue(BlockStateProperties.OPEN);
        Direction facing = newState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide hinge = newState.getValue(BlockStateProperties.DOOR_HINGE);

        Direction neighborDir = (hinge == DoorHingeSide.RIGHT) ? facing.getCounterClockWise() : facing.getClockWise();
        BlockPos neighborPos = pos.relative(neighborDir);
        BlockState neighborState = level.getBlockState(neighborPos);

        if (neighborState.is(newState.getBlock())
                && neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
                && neighborState.getValue(BlockStateProperties.DOOR_HINGE) != hinge
                && neighborState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == newState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && !(neighborState.hasProperty(NeatlyBetter.GLUED)
                && neighborState.getValue(NeatlyBetter.GLUED))) {

            if (neighborState.getValue(BlockStateProperties.OPEN) != isNowOpen) {
                level.setBlock(neighborPos, neighborState.setValue(BlockStateProperties.OPEN, isNowOpen), 10);
                level.gameEvent(player, isNowOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, neighborPos);
            }
        }
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/DoorBlock;registerDefaultState" +
                            "(Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private void neatlybetter$wrapSetDefaultState(DoorBlock instance, BlockState blockState, Operation<Void> original) {
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