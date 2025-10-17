package net.hallowed.oldways.mixin.entity.passive;

import net.fabricmc.loader.api.FabricLoader;
import net.hallowed.oldways.api.OWCompat;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin {

    @Unique private long lastTimeInWater = Long.MIN_VALUE / 4;
    @Unique private static final long SWIM_EXIT_COOLDOWN_TICKS = 8L;

    @Inject(method = "getControlledMovementInput", at = @At("RETURN"))
    private void addSwimUpwardMotion(net.minecraft.entity.player.PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfoReturnable<Vec3d> cir) {
        if (OWCompat.HORSEMAN) return;
        AbstractHorseEntity horse = (AbstractHorseEntity) (Object) this;
        World world = horse.getEntityWorld();
        if (controllingPlayer.isJumping() && horse.isTouchingWater() && horse.isTame() && horse.getFluidHeight(FluidTags.WATER) > 0.0) {
            Vec3d velocity = horse.getVelocity();
            horse.setVelocity(velocity.add(0.0, 0.04, 0.0));
        }
        if (horse.getFluidHeight(FluidTags.WATER) > 0.0) {
            this.lastTimeInWater = world.getTime();
        }
    }

    @Inject(method = "canJump", at = @At("HEAD"), cancellable = true)
    private void disallowJumpWhileSwimming(CallbackInfoReturnable<Boolean> cir) {
        if (OWCompat.HORSEMAN) return;
        AbstractHorseEntity horse = (AbstractHorseEntity) (Object) this;
        World world = horse.getEntityWorld();

        if (!(horse.isTame() && horse.getControllingPassenger() instanceof PlayerEntity)) {
            return;
        }
        if (horse.getFluidHeight(FluidTags.WATER) > 0.0) {
            cir.setReturnValue(false);
            return;
        }
        if (this.lastTimeInWater == Long.MIN_VALUE / 4) {
            return;
        }
        long elapsed = world.getTime() - this.lastTimeInWater;
        if (elapsed < SWIM_EXIT_COOLDOWN_TICKS) {
            cir.setReturnValue(false);
        }
    }

}
