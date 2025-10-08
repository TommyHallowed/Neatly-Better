package net.hallowed.oldways.mixin.entity.misc;

import net.hallowed.oldways.mixin.accessor.TridentEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
abstract class TridentEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void oldways$returnFromVoid(CallbackInfo ci) {
        TridentEntity self = (TridentEntity) (Object) this;
        DataTracker tracker = self.getDataTracker();

        byte loyalty = tracker.get(TridentEntityAccessor.oldways$getLoyaltyTrackedData());
        if (loyalty <= 0) return;

        Entity owner = self.getOwner();
        if (!(owner instanceof PlayerEntity player) || !player.isAlive()) return;

        if (self.getY() < self.getWorld().getBottomY()) {
            self.setNoClip(true);
            self.onPlayerCollision(player);
        }
    }
}
