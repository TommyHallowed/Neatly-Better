package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.api.LinkableMinecart;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.util.CartUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {

    protected NewMinecartBehaviorMixin(AbstractMinecart abstractMinecart) {
        super(abstractMinecart);
    }

    @Inject(at = @At("HEAD"), method = "getMaxSpeed", cancellable = true)
    public void neatlybetter$getMaxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        AbstractMinecart following = ((LinkableMinecart) this.minecart).neatlybetter$getFollowing();
        if (following != null) {
            cir.setReturnValue(following.getMaxSpeed(level));
            return;
        }

        double waterFactor = this.minecart.isInWater() ? 0.5 : 1.0;
        double blocksPerSecond;

        Entity passenger = minecart.getFirstPassenger();
        if (passenger == null && minecart.isRideable()) {
            blocksPerSecond = NTServerConfig.CONFIG.minecartEmptySpeed.get();
        } else if (!minecart.isRideable()) {
            blocksPerSecond = NTServerConfig.CONFIG.minecartOtherSpeed.get();
        } else if (passenger instanceof Player) {
            blocksPerSecond = NTServerConfig.CONFIG.minecartPlayerSpeed.get();
        }
        else {
            blocksPerSecond = NTServerConfig.CONFIG.minecartPassengerSpeed.get();
        }

        cir.setReturnValue(Math.max(0.0, blocksPerSecond) * waterFactor / 20.0);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void neatlybetter$tickChainFollow(CallbackInfo ci) {

        AbstractMinecart following = ((LinkableMinecart) this.minecart).neatlybetter$getFollowing();
        if (following == null) return;

        if (following.isRemoved()) {
            CartUtils.unlinkFromParent(this.minecart);
            return;
        }

        Vec3 myPos = this.minecart.position();
        Vec3 targetPos = following.position();

        double linkDistance = 1.0;
        Vec3 vec3d = targetPos.subtract(myPos);
        double centerDist = vec3d.length();
        double dist = Math.max(centerDist - linkDistance, 0.0);

        Vec3 myVel = this.minecart.getDeltaMovement();
        Vec3 targetVel = following.getDeltaMovement();

        // Squared thresholds: 0.15², 0.005², 1.42²
        boolean differentDirection = centerDist > 0.5
                && myVel.lengthSqr() > 0.0225 && targetVel.lengthSqr() > 0.000025
                && myVel.normalize().distanceToSqr(targetVel.normalize()) > 2.0164;

        if (differentDirection) {
            dist += linkDistance;
            vec3d = myVel;
        }

        if (vec3d.lengthSqr() > 0) {
            vec3d = vec3d.normalize().scale(dist);
        }

        if (dist <= 1.0) {
            this.minecart.setDeltaMovement(vec3d.scale(0.8 + 0.2 * Math.abs(dist)));
        } else if (dist <= 8.0) {
            this.minecart.setDeltaMovement(vec3d);
        } else {
            CartUtils.unlinkFromParent(this.minecart);
        }
    }
}