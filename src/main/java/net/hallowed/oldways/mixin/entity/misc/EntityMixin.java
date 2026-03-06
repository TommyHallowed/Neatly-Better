package net.hallowed.oldways.mixin.entity.misc;

import net.hallowed.oldways.api.LinkableMinecart;
import net.hallowed.oldways.util.CartUtils;
import net.hallowed.oldways.util.CollisionUtils;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    void oldways$removeLink(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this instanceof AbstractMinecartEntity minecart) {
            if (!minecart.getEntityWorld().isClient() && reason.shouldDestroy()) {
                CartUtils.unlinkFromParent(minecart);
                CartUtils.unlinkFromParent(((LinkableMinecart) minecart).oldways$getFollower());
            }
        }
    }

    @Inject(method = "adjustMovementForCollisions*", at = @At("HEAD"), cancellable = true)
    void oldways$onRecalculateVelocity(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        if ((Object) this instanceof AbstractMinecartEntity minecart) {
            for (Entity entity : minecart.getEntityWorld().getOtherEntities(minecart, minecart.getBoundingBox().offset(movement))) {
                if (!CollisionUtils.shouldCollide(minecart, entity) && minecart.getEntityWorld().getBlockState(minecart.getBlockPos()).getBlock() instanceof AbstractRailBlock) {
                    cir.setReturnValue(movement);
                    return;
                }
            }
        }
    }
}