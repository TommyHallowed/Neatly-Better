package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void neatlybetter$syncDoubleDoors(BlockState originalState, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {

        if (!cir.getReturnValue().consumesAction()) {
            return;
        }

        BlockState newState = world.getBlockState(pos);
        if (!(newState.getBlock() instanceof DoorBlock)) return;

        boolean isNowOpen = newState.getValue(BlockStateProperties.OPEN);
        Direction facing = newState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide hinge = newState.getValue(BlockStateProperties.DOOR_HINGE);

        Direction neighborDir = (hinge == DoorHingeSide.RIGHT) ? facing.getCounterClockWise() : facing.getClockWise();
        BlockPos neighborPos = pos.relative(neighborDir);
        BlockState neighborState = world.getBlockState(neighborPos);

        if (neighborState.is(newState.getBlock())
                && neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
                && neighborState.getValue(BlockStateProperties.DOOR_HINGE) != hinge
                && neighborState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == newState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)) {

            if (neighborState.getValue(BlockStateProperties.OPEN) != isNowOpen) {
                world.setBlock(neighborPos, neighborState.setValue(BlockStateProperties.OPEN, isNowOpen), 10);
                world.gameEvent(player, isNowOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, neighborPos);
            }
        }
    }
}