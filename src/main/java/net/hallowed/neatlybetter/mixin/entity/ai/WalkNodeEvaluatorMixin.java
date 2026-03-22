package net.hallowed.neatlybetter.mixin.entity.ai;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin {

    @Inject(method = "getPathTypeFromState", at = @At("RETURN"), cancellable = true)
    private static void neatlybetter$fenceGatePathType(BlockGetter level, BlockPos pos,
                                                       CallbackInfoReturnable<PathType> cir) {
    if (NTServerConfig.CONFIG_SPEC.isLoaded() && NTServerConfig.CONFIG.villagerOpensFenceGate.get()) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FenceGateBlock && !state.getValue(FenceGateBlock.OPEN)) {
            cir.setReturnValue(PathType.DOOR_WOOD_CLOSED);
        }
    }
}

    @Inject(method = "prepare", at = @At("TAIL"))
    private void neatlybetter$ensureVillagerDoorFlags(PathNavigationRegion region, Mob mob, CallbackInfo ci) {
            if (NTServerConfig.CONFIG_SPEC.isLoaded() && NTServerConfig.CONFIG.villagerOpensFenceGate.get()) {
            if (mob instanceof Villager) {
                ((WalkNodeEvaluator)(Object)this).setCanOpenDoors(true);
                ((WalkNodeEvaluator)(Object)this).setCanPassDoors(true);
            }
        }
    }
}
