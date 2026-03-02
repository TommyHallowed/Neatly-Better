package net.hallowed.oldways.mixin.block;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.block.SpongeBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SpongeBlock.class)
public abstract class SpongeBlockMixin {

    @Unique
    private static int oldways$getRadius(World world) {
        if (world instanceof ServerWorld sw) {
            return Math.max(1, sw.getGameRules().getValue(ModGameRules.SPONGE_BLOCK_ABSORB_RADIUS));
        }
        return 6;
    }

    @ModifyConstant(method = "absorbWater", constant = @Constant(intValue = 6))
    private int oldways$radiusFromRule(int original, World world, BlockPos pos) {
        return oldways$getRadius(world);
    }

    @ModifyConstant(method = "absorbWater", constant = @Constant(intValue = 65))
    private int oldways$limitFromRule(int original, World world, BlockPos pos) {
        int r = oldways$getRadius(world);
        double scale = Math.pow(r / 6.0D, 3.0D);
        return (int) Math.max(1, Math.round(65 * scale));
    }
}
