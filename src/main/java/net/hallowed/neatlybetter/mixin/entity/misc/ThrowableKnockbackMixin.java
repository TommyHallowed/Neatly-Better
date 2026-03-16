package net.hallowed.neatlybetter.mixin.entity.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Snowball.class, ThrownEgg.class})
public abstract class ThrowableKnockbackMixin {
    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void neatlybetter$knockbackPlayers(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide()) return;

        Entity target = entityHitResult.getEntity();
        if (target instanceof ServerPlayer player
                && !player.isCreative()
                && !player.isSpectator()) {
            Vec3 direction = self.getDeltaMovement().normalize();
            player.knockback(0.2, -direction.x, -direction.z);
            player.hurtMarked = true;
        }
    }
}
