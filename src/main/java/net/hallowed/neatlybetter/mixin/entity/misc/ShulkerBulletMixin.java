package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void neatlybetter$despawnWhenShulkerOwnerDies(CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.shulkerBulletDespawnsAfterOwner.get()) return;
        ShulkerBullet self = (ShulkerBullet)(Object)this;
        if (self.level().isClientSide()) return;

        Entity owner = self.getOwner();
        if (owner instanceof Shulker && !owner.isAlive()) {
            self.discard();
        }
    }
}
