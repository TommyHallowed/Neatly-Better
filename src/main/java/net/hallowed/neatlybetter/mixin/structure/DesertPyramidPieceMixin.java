package net.hallowed.neatlybetter.mixin.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.util.RandomSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DesertPyramidPiece.class)
public abstract class DesertPyramidPieceMixin extends StructurePiece {

    protected DesertPyramidPieceMixin() {
        super(StructurePieceType.DESERT_PYRAMID_PIECE, 0, new BoundingBox(0, 0, 0, 0, 0, 0));
    }

    @Inject(
            method = "postProcess",
            at = @At("RETURN")
    )
    private void replacePressurePlate(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBB,
            ChunkPos chunkPos,
            BlockPos referencePos,
            CallbackInfo ci) {
        BlockPos platePos = this.getWorldPos(10, -11, 10);
        if (chunkBB.isInside(platePos) && level.getBlockState(platePos).is(Blocks.STONE_PRESSURE_PLATE)) {
            level.setBlock(platePos, Blocks.BIRCH_PRESSURE_PLATE.defaultBlockState(), 3);
        }
    }
}