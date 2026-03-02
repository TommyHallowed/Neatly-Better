package net.hallowed.oldways.mixin.item;

import net.hallowed.oldways.init.ModGameRules;
import net.hallowed.oldways.init.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void oldways$onlyDragonBurstBoosts(World world, PlayerEntity user, Hand hand,
                                               CallbackInfoReturnable<ActionResult> cir) {
        if (!(user.getEntityWorld() instanceof ServerWorld sw)) return;
        if (sw.getGameRules().getValue(ModGameRules.DO_ELYTRA_FIREWORK_BOOSTING)) return;
        if (!user.isGliding()) return;
        ItemStack stack = user.getStackInHand(hand);

        if (stack.isOf(ModItems.DRAGON_BURST_ROCKET)) return;
        cir.setReturnValue(ActionResult.PASS);
    }
}
