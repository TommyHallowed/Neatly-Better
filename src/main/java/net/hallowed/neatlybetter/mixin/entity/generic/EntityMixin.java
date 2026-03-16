package net.hallowed.neatlybetter.mixin.entity.generic;

import net.hallowed.neatlybetter.api.LinkableMinecart;
import net.hallowed.neatlybetter.util.CartUtils;
import net.hallowed.neatlybetter.util.CollisionUtils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    void neatlybetter$removeLink(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this instanceof AbstractMinecart minecart) {
            if (!minecart.level().isClientSide() && reason.shouldDestroy()) {
                CartUtils.unlinkFromParent(minecart);
                CartUtils.unlinkFromParent(((LinkableMinecart) minecart).neatlybetter$getFollower());
            }
        }
    }

    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    void neatlybetter$onRecalculateVelocity(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this instanceof AbstractMinecart minecart) {
            for (Entity entity : minecart.level().getEntities(minecart, minecart.getBoundingBox().move(movement))) {
                if (!CollisionUtils.shouldCollide(minecart, entity) && minecart.level().getBlockState(minecart.blockPosition()).getBlock() instanceof BaseRailBlock) {
                    cir.setReturnValue(movement);
                    return;
                }
            }
        }
    }
}