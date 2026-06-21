package net.hallowed.neatlybetter.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue);

    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    public void preventReequipWhenUsing(InteractionHand hand, CallbackInfo callback) {
        if (!NTCommonConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        assert this.minecraft.player != null;
        if (this.minecraft.player.isUsingItem() && this.minecraft.player.getUsedItemHand() == hand) {
            callback.cancel();
        }
    }

    @Inject(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderInteractAnimations(AbstractClientPlayer player, float frameInterp, float xRot,
                                          InteractionHand hand, float attack, ItemStack itemStack,
                                          float inverseArmHeight, PoseStack poseStack,
                                          SubmitNodeCollector submitNodeCollector, int lightCoords,
                                          CallbackInfo callback) {
        if (!NTCommonConfig.CONFIG.legacyCombat.get()) {
            return;
        }
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                    ? player.getMainArm()
                    : player.getMainArm().getOpposite();
            this.applyItemArmAttackTransform(poseStack, arm, attack);
        }
    }
}
