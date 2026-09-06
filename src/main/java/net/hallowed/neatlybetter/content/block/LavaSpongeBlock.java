package net.hallowed.neatlybetter.content.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.hallowed.neatlybetter.init.ModBlocks;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LavaSpongeBlock extends Block {

    public static final MapCodec<LavaSpongeBlock> CODEC = simpleCodec(LavaSpongeBlock::new);

    private static final int MAX_DEPTH = 10;
    private static final int MAX_COUNT = (int) Math.max(1, Math.round(65 * Math.pow(MAX_DEPTH / 6.0D, 3.0D)));
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    @Override
    public @NonNull MapCodec<LavaSpongeBlock> codec() {
        return CODEC;
    }

    public LavaSpongeBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(final BlockState state, final @NonNull Level level, final @NonNull BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            this.tryAbsorbLava(level, pos);
        }
    }

    @Override
    protected void neighborChanged(
            final @NonNull BlockState state, final @NonNull Level level, final @NonNull BlockPos pos, final @NonNull Block block, final @Nullable Orientation orientation, final boolean movedByPiston
    ) {
        this.tryAbsorbLava(level, pos);
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }

    protected void tryAbsorbLava(final Level level, final BlockPos pos) {
        if (this.removeLavaBreadthFirstSearch(level, pos)) {
            level.setBlock(pos, ModBlocks.SOAKED_LAVA_SPONGE.defaultBlockState(), 2);
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private boolean removeLavaBreadthFirstSearch(final Level level, final BlockPos startPos) {
        return BlockPos.breadthFirstTraversal(startPos, MAX_DEPTH, MAX_COUNT, (pos, consumer) -> {
            for (Direction direction : ALL_DIRECTIONS) {
                consumer.accept(pos.relative(direction));
            }
        }, pos -> {
            if (pos.equals(startPos)) {
                return BlockPos.TraversalNodeStatus.ACCEPT;
            } else {
                BlockState state = level.getBlockState(pos);
                FluidState fluidState = level.getFluidState(pos);
                if (!fluidState.is(FluidTags.LAVA)) {
                    return BlockPos.TraversalNodeStatus.SKIP;
                } else if (state.getBlock() instanceof BucketPickup bucketPickup && !bucketPickup.pickupBlock(null, level, pos, state).isEmpty()) {
                    return BlockPos.TraversalNodeStatus.ACCEPT;
                } else {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    return BlockPos.TraversalNodeStatus.ACCEPT;
                }
            }
        }) > 1;
    }
}