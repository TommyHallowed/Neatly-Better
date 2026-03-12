package net.hallowed.oldways.mixin.compat;

import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks {@link BackpackInventory#setChanged()} so that whenever a slot inside
 * the backpack is modified the owning player is marked dirty for a one-shot
 * state push on the next server tick.
 */
@Mixin(BackpackInventory.class)
public abstract class BackpackInventoryMixin {

    @Shadow @Final private Player player;

    @Inject(method = "setChanged", at = @At("TAIL"))
    private void oldways$onSetChanged(CallbackInfo ci) {
        if (this.player instanceof ServerPlayer sp) {
            OldWaysNetwork.markBackpackDirty(sp);
        }
    }
}
