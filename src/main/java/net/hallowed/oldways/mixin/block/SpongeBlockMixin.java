package net.hallowed.oldways.mixin.block;

import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpongeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SpongeBlock.class)
public abstract class SpongeBlockMixin {

    @Unique
    private static int oldways$getRadius(Level world) {
        if (world instanceof ServerLevel sw) {
            return Math.max(1, sw.getGameRules().get(ModGameRules.SPONGE_BLOCK_ABSORB_RADIUS));
        }
        return 6;
    }

    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 6))
    private int oldways$radiusFromRule(int original, Level world, BlockPos pos) {
        return oldways$getRadius(world);
    }

    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 65))
    private int oldways$limitFromRule(int original, Level world, BlockPos pos) {
        int r = oldways$getRadius(world);
        double scale = Math.pow(r / 6.0D, 3.0D);
        return (int) Math.max(1, Math.round(65 * scale));
    }
}
