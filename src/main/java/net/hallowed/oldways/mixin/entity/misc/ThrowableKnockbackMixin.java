package net.hallowed.oldways.mixin.entity.misc;

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

/**
 * Applies knockback to players when hit by snowballs or thrown eggs.

 * Vanilla only reliably knocks back non-player entities because the
 * 0-damage hit doesn't trigger the velocity sync packet for players.
 * This fixes it by calling knockback + hurtMarked to ensure the
 * client receives the velocity update.
 */
@Mixin({Snowball.class, ThrownEgg.class})
public abstract class ThrowableKnockbackMixin {
    /**
     * Fires after vanilla's onHitEntity finishes.
     * Applies directional knockback to players based on projectile velocity,
     * then flags hurtMarked so the server sends the velocity packet to the client.
     */
    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void oldways$knockbackPlayers(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide()) return;

        Entity target = entityHitResult.getEntity();
        if (target instanceof ServerPlayer player
                && !player.isCreative()
                && !player.isSpectator()) {
            Vec3 direction = self.getDeltaMovement().normalize();
            player.knockback(0.4, -direction.x, -direction.z);
            player.hurtMarked = true;
        }
    }
}
