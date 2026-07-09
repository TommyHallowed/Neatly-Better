package net.hallowed.neatlybetter.content.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GlowWallTorchBlock extends WallTorchBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<WallTorchBlock> CODEC = RecordCodecBuilder.mapCodec((i) ->
            i.group(PARTICLE_OPTIONS_FIELD.forGetter((b) -> ParticleTypes.GLOW), propertiesCodec())
                    .apply(i, GlowWallTorchBlock::new));

    public GlowWallTorchBlock(final SimpleParticleType flameParticle, final BlockBehaviour.Properties properties) {
        super(flameParticle, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public @NonNull MapCodec<WallTorchBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final @NonNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).is(FluidTags.WATER);
        return state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
    }

    @Override
    protected @NonNull FluidState getFluidState(final BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final @NonNull RandomSource random) {
        Direction direction = state.getValue(FACING);
        double x = (double)pos.getX() + (double)0.5F;
        double y = (double)pos.getY() + 0.7;
        double z = (double)pos.getZ() + (double)0.5F;
        double h = 0.22;
        double r = 0.27;
        Direction opposite = direction.getOpposite();
        level.addParticle(ParticleTypes.GLOW, x + 0.27 * (double)opposite.getStepX(), y + 0.22, z + 0.27 * (double)opposite.getStepZ(), 0.0F, 0.0F, 0.0F);
    }

    @Override
    protected @NonNull BlockState updateShape(final BlockState state, final @NonNull LevelReader level, final @NonNull ScheduledTickAccess ticks, final @NonNull BlockPos pos, final @NonNull Direction directionToNeighbour, final @NonNull BlockPos neighbourPos, final @NonNull BlockState neighbourState, final @NonNull RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }
}