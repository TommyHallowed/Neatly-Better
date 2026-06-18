package net.hallowed.neatlybetter.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class CopperGrateHelper {
    private static final List<Block> copperGrates = Blocks.COPPER_GRATE.asList();

    public static boolean isWaterloggedCopperGrate(BlockState state) {
        return copperGrates.stream().anyMatch(state::is) && state.hasProperty(BlockStateProperties.WATERLOGGED);
    }
}