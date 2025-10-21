package net.hallowed.oldways.content.block;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class GlowTorchBlock extends TorchBlock implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public GlowTorchBlock(AbstractBlock.Settings settings) {
        super(ParticleTypes.FLAME, settings);
        this.setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState base = super.getPlacementState(ctx);
        if (base == null) return null;

        BlockPos pos = ctx.getBlockPos();
        int sourceCount = 0;

        for (Direction d : Direction.values()) {
            BlockPos check = pos.offset(d);
            FluidState fs = ctx.getWorld().getFluidState(check);
            if (fs.isIn(FluidTags.WATER) && fs.isStill()) {
                sourceCount++;
                if (sourceCount >= 2) break;
            }
        }

        boolean waterlogged = sourceCount == 2;
        return base.with(WATERLOGGED, waterlogged);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView ticks,
                                                   BlockPos pos, net.minecraft.util.math.Direction dir,
                                                   BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            ticks.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, ticks, pos, dir, neighborPos, neighborState, random);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rng) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;
        world.addParticleClient(ParticleTypes.GLOW, x, y, z, 0.0, 0.005, 0.0);
    }
}
