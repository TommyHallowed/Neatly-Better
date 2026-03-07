package net.hallowed.oldways.mixin.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashPotionItem.class)
abstract class SplashPotionItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void oldways$cooldown(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!user.getAbilities().instabuild) {
            ItemStack stack = user.getItemInHand(hand);
            user.getCooldowns().addCooldown(stack, 20);
        }
    }
}
