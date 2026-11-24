package net.hallowed.neatlybetter.client.mixin.screen;

import net.minecraft.client.resources.model.sprite.SpriteId;
import net.hallowed.neatlybetter.feature.combat.SoulFire;
import net.hallowed.neatlybetter.client.feature.soulfire.SoulFireRendering;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
    private static void neatlybetter$hideFireOverlayWithFireResistance(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, TextureAtlasSprite sprite, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            ci.cancel();
        }
    }

    /** First-person fire overlay: soul fire sprite while the local player burns with soul fire. */
    @ModifyExpressionValue(
            method = "submit",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/resources/model/ModelBakery;FIRE_1:Lnet/minecraft/client/resources/model/sprite/SpriteId;")
    )
    private SpriteId neatlybetter$soulFireOverlay(SpriteId original) {
        var player = Minecraft.getInstance().player;
        return player != null && SoulFire.isSoulBurning(player) ? SoulFireRendering.SOUL_FIRE_1 : original;
    }
}
