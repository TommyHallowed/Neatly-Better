package net.hallowed.neatlybetter.mixin.block;

import net.hallowed.neatlybetter.config.NTServerConfig;

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
    private static int neatlybetter$getRadius(Level world) {
        if (world instanceof ServerLevel) {
            return Math.max(1, NTServerConfig.CONFIG.spongeBlockAbsorbRadius.get());
        }
        return 6;
    }

    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 6))
    private int neatlybetter$radiusFromRule(int original, Level world, BlockPos pos) {
        return neatlybetter$getRadius(world);
    }

    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 65))
    private int neatlybetter$limitFromRule(int original, Level world, BlockPos pos) {
        int r = neatlybetter$getRadius(world);
        double scale = Math.pow(r / 6.0D, 3.0D);
        return (int) Math.max(1, Math.round(65 * scale));
    }
}
