package net.hallowed.neatlybetter.content.block;

import net.hallowed.neatlybetter.init.ModCauldronInteractions;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MilkCauldronBlock extends LayeredCauldronBlock {

    public MilkCauldronBlock(BlockBehaviour.Properties properties) {
        super(Biome.Precipitation.NONE, ModCauldronInteractions.MILK, properties);
    }
}