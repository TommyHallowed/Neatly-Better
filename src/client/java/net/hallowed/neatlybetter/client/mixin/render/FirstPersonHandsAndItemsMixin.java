package net.hallowed.neatlybetter.client.mixin.render;

import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public abstract class FirstPersonHandsAndItemsMixin {

    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    public void preventReequipWhenUsing(InteractionHand hand, CallbackInfo callback) {
        if (!NTCommonConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.isUsingItem() && player.getUsedItemHand() == hand) {
            callback.cancel();
        }
    }
}