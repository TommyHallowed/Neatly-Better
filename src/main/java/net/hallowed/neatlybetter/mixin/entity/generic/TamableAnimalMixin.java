package net.hallowed.neatlybetter.mixin.entity.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {

    @Inject(method = "shouldTryTeleportToOwner", at = @At("HEAD"), cancellable = true)
    private void increaseTeleportDistance(CallbackInfoReturnable<Boolean> cir) {
        TamableAnimal self = (TamableAnimal) (Object) this;
        LivingEntity owner = self.getOwner();
        cir.setReturnValue(owner != null && self.distanceToSqr(owner) >= 720);
    }

    @Inject(method = "canTeleportTo", at = @At("RETURN"), cancellable = true)
    private void allowTeleportToOwnerInWater(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;

        TamableAnimal self = (TamableAnimal) (Object) this;
        LivingEntity owner = self.getOwner();

        if (owner == null || !owner.isInWater() || owner.isUnderWater()) return;

        PathType pathType = WalkNodeEvaluator.getPathTypeStatic(self, pos);
        if (pathType != PathType.WATER && pathType != PathType.WATER_BORDER) return;

        BlockPos delta = pos.subtract(self.blockPosition());
        if (self.level().noCollision(self, self.getBoundingBox().move(delta))) {
            cir.setReturnValue(true);
        }
    }
}