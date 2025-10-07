package net.hallowed.oldways.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapState.class)
public class MapStateMixin {
    @Shadow @Final @Mutable
    private boolean unlimitedTracking;

    @Inject(
            method = "getPlayerSyncData(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/map/MapState$PlayerUpdateTracker;",
            at = @At("HEAD")
    )
    private void oldways$enableUnlimitedTracking(PlayerEntity player, CallbackInfoReturnable<MapState.PlayerUpdateTracker> cir) {
        this.unlimitedTracking = true;
    }
}
