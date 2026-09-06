package net.hallowed.neatlybetter.content.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class SoakedLavaSpongeBlock extends Block {

    public static final MapCodec<SoakedLavaSpongeBlock> CODEC = simpleCodec(SoakedLavaSpongeBlock::new);

    @Override
    public @NonNull MapCodec<SoakedLavaSpongeBlock> codec() {
        return CODEC;
    }

    public SoakedLavaSpongeBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(final @NonNull BlockState state, final @NonNull Level level, final @NonNull BlockPos pos, final @NonNull RandomSource random) {
        Direction direction = Direction.getRandom(random);
        if (direction != Direction.UP) {
            BlockPos relativePos = pos.relative(direction);
            BlockState blockState = level.getBlockState(relativePos);
            if (!state.canOcclude() || !blockState.isFaceSturdy(level, relativePos, direction.getOpposite())) {
                double xx = pos.getX();
                double yy = pos.getY();
                double zz = pos.getZ();
                if (direction == Direction.DOWN) {
                    yy -= 0.05;
                    xx += random.nextDouble();
                    zz += random.nextDouble();
                } else {
                    yy += random.nextDouble() * 0.8;
                    if (direction.getAxis() == Direction.Axis.X) {
                        zz += random.nextDouble();
                        if (direction == Direction.EAST) {
                            xx += 1.1;
                        } else {
                            xx += 0.05;
                        }
                    } else {
                        xx += random.nextDouble();
                        if (direction == Direction.SOUTH) {
                            zz += 1.1;
                        } else {
                            zz += 0.05;
                        }
                    }
                }

                level.addParticle(ParticleTypes.DRIPPING_LAVA, xx, yy, zz, 0.0, 0.0, 0.0);
            }
        }
    }
}