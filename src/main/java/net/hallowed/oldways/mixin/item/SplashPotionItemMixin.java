package net.hallowed.oldways.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashPotionItem.class)
abstract class SplashPotionItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void oldways$cooldown(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!user.getAbilities().creativeMode) {
            ItemStack stack = user.getStackInHand(hand);
            user.getItemCooldownManager().set(stack, 20);
        }
    }
}
