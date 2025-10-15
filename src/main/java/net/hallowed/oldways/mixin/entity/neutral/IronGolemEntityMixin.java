package net.hallowed.oldways.mixin.entity.neutral;

import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolemEntity.class)
public abstract class IronGolemEntityMixin {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void oldways$replaceRepairItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        IronGolemEntity self = (IronGolemEntity) (Object) this;
        if (stack.isOf(Items.IRON_INGOT)) {
            cir.setReturnValue(ActionResult.PASS);
            return;
        }

        if (stack.isOf(Items.IRON_BLOCK)) {
            float before = self.getHealth();
            self.heal(50.0F);

            if (self.getHealth() == before) {
                cir.setReturnValue(ActionResult.PASS);
                return;
            }

            float pitch = 1.0F + (self.getRandom().nextFloat() - self.getRandom().nextFloat()) * 0.2F;
            self.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0F, pitch);

            stack.decrementUnlessCreative(1, player);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
