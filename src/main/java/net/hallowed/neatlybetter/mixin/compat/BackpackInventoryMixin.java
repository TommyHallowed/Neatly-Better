package net.hallowed.neatlybetter.mixin.compat;

//import com.mrcrayfish.backpacked.inventory.BackpackInventory;

import net.hallowed.neatlybetter.network.NTNetwork;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


//@Mixin(BackpackInventory.class)
public abstract class BackpackInventoryMixin {

//    @Shadow @Final private Player player;

//    @Inject(method = "setChanged", at = @At("TAIL"))
//    private void neatlybetter$onSetChanged(CallbackInfo ci) {
//        if (this.player instanceof ServerPlayer sp) {
//            NTNetwork.markBackpackDirty(sp);
//        }
//    }
}
