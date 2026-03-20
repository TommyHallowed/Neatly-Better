package net.hallowed.neatlybetter.mixin.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {
    @Shadow @Final @Mutable
    private boolean unlimitedTracking;

    @Inject(
            method = "getHoldingPlayer(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer;",
            at = @At("HEAD")
    )
    private void neatlybetter$enableUnlimitedTracking(Player player, CallbackInfoReturnable<MapItemSavedData.HoldingPlayer> cir) {
        this.unlimitedTracking = true;
    }
}
