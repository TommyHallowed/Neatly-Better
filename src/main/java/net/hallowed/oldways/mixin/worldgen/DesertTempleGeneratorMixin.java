package net.hallowed.oldways.mixin.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.structure.DesertTempleGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DesertTempleGenerator.class)
public abstract class DesertTempleGeneratorMixin {

    @Redirect(
            method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/minecraft/block/Blocks;STONE_PRESSURE_PLATE:Lnet/minecraft/block/Block;"
            )
    )
    private Block oldways$swapStonePlateForBirch() {
        return Blocks.BIRCH_PRESSURE_PLATE;
    }
}
