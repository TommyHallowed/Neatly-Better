package net.hallowed.oldways.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin {

    @Inject(method = "onUse", at = @At("RETURN"))
    private void oldways$syncDoubleDoors(BlockState originalState, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

        // .isAccepted() covers SUCCESS, CONSUME, etc. Safest way to check if interaction worked!
        if (!cir.getReturnValue().isAccepted()) {
            return;
        }

        // Fetch the ACTUAL new state directly from the world
        BlockState newState = world.getBlockState(pos);
        if (!(newState.getBlock() instanceof DoorBlock)) return;

        boolean isNowOpen = newState.get(Properties.OPEN);
        Direction facing = newState.get(Properties.HORIZONTAL_FACING);
        DoorHinge hinge = newState.get(Properties.DOOR_HINGE);

        // Determine where the matching door should be based on the hinge placement
        Direction neighborDir = (hinge == DoorHinge.RIGHT) ? facing.rotateYCounterclockwise() : facing.rotateYClockwise();
        BlockPos neighborPos = pos.offset(neighborDir);
        BlockState neighborState = world.getBlockState(neighborPos);

        if (neighborState.isOf(newState.getBlock())
                && neighborState.get(Properties.HORIZONTAL_FACING) == facing
                && neighborState.get(Properties.DOOR_HINGE) != hinge
                && neighborState.get(Properties.DOUBLE_BLOCK_HALF) == newState.get(Properties.DOUBLE_BLOCK_HALF)) {

            if (neighborState.get(Properties.OPEN) != isNowOpen) {
                // Manually set the state and emit the game event WITHOUT playing the sound!
                // The '10' is the standard block update flag (2 for block update + 8 for client sync)
                world.setBlockState(neighborPos, neighborState.with(Properties.OPEN, isNowOpen), 10);
                world.emitGameEvent(player, isNowOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, neighborPos);
            }
        }
    }
}