package net.hallowed.neatlybetter.mixin.item;

import net.hallowed.neatlybetter.init.ModGameRules;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$onlyDragonBurstBoosts(Level world, Player user, InteractionHand hand,
                                                    CallbackInfoReturnable<InteractionResult> cir) {
        if (!user.isFallFlying()) return;
        MinecraftServer server = world.getServer();
        if (server == null) return;

        if (server.overworld().getGameRules().get(ModGameRules.DO_ELYTRA_FIREWORK_BOOSTING)) return;
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
