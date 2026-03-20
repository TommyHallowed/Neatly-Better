package net.hallowed.neatlybetter.mixin.entity.neutral;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolem.class)
public abstract class IronGolemEntityMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$replaceRepairItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!NTServerConfig.CONFIG.ironGolemRepairUsingBlocks.get()) return;
        ItemStack stack = player.getItemInHand(hand);
        IronGolem self = (IronGolem) (Object) this;
        if (stack.is(Items.IRON_INGOT)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (stack.is(Items.IRON_BLOCK)) {
            float before = self.getHealth();
            self.heal(50.0F);

            if (self.getHealth() == before) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            float pitch = 1.0F + (self.getRandom().nextFloat() - self.getRandom().nextFloat()) * 0.2F;
            self.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, pitch);

            stack.consume(1, player);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
