package net.hallowed.neatlybetter.mixin.entity.misc;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
abstract class TridentEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void neatlybetter$returnFromVoid(CallbackInfo ci) {
        ThrownTrident self = (ThrownTrident) (Object) this;
        SynchedEntityData tracker = self.getEntityData();

        byte loyalty = tracker.get(ThrownTrident.ID_LOYALTY);
        if (loyalty <= 0) return;

        Entity owner = self.getOwner();
        if (!(owner instanceof Player player) || !player.isAlive()) return;

        if (self.getY() < self.level().getMinY()) {
            self.setNoPhysics(true);
            self.playerTouch(player);
        }
    }
}
