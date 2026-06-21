package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Right-clicking a ladder while holding a ladder in the main hand extends the
 * pillar: scans to the far end of the connected ladder run (up normally,
 * down while sneaking) and places one more ladder past it, if that spot is air.
 */
public final class LadderPillarExtender {

    private LadderPillarExtender() {}

    public static void register() {
        UseBlockCallback.EVENT.register(LadderPillarExtender::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(Items.LADDER)) {
            return InteractionResult.PASS;
        }

        if (player.isSpectator() || !player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = hitResult.getBlockPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(Blocks.LADDER)) {
            return InteractionResult.PASS;
        }

        Direction direction = player.isShiftKeyDown() ? Direction.DOWN : Direction.UP;

        // Walk to the far end of the connected ladder run, tracking the
        // last actual ladder block so we can copy its FACING.
        BlockPos pos = clickedPos;
        BlockState lastLadderState = clickedState;
        while (true) {
            BlockPos next = pos.relative(direction);
            BlockState nextState = level.getBlockState(next);
            if (!nextState.is(Blocks.LADDER)) {
                pos = next;
                break;
            }
            pos = next;
            lastLadderState = nextState;
        }

        if (level.isOutsideBuildHeight(pos)) {
            return InteractionResult.PASS;
        }

        if (!level.getBlockState(pos).isAir()) {
            return InteractionResult.PASS;
        }

        BlockState placeState = lastLadderState.setValue(LadderBlock.WATERLOGGED, false);

        level.setBlock(pos, placeState, 3);

        SoundType soundType = placeState.getSoundType();
        level.playSound(player, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        if (!player.getAbilities().instabuild) {
            mainHand.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}