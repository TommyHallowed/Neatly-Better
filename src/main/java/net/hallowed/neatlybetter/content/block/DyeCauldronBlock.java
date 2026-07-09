package net.hallowed.neatlybetter.content.block;

import net.hallowed.neatlybetter.content.blockentity.DyeCauldronBlockEntity;
import net.hallowed.neatlybetter.init.ModCauldronInteractions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DyeCauldronBlock extends LayeredCauldronBlock implements EntityBlock {

    public DyeCauldronBlock(BlockBehaviour.Properties properties) {
        super(Biome.Precipitation.NONE, ModCauldronInteractions.DYE, properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new DyeCauldronBlockEntity(pos, state);
    }
}