package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {

    protected NewMinecartBehaviorMixin(AbstractMinecart abstractMinecart) {
        super(abstractMinecart);
    }

    @Inject(at = @At("HEAD"), method = "getMaxSpeed", cancellable = true)
    public void neatlybetter$getMaxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
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
}