package net.hallowed.neatlybetter.client.mixin.render.banner;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.hallowed.neatlybetter.client.render.EmissiveBannerState;
import net.hallowed.neatlybetter.init.ModDataComponents;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.BannerSpecialRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(BannerSpecialRenderer.class)
public abstract class BannerSpecialRendererMixin {

    @Unique
    private boolean neatlybetter$pendingEmissive;

    @Inject(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/level/block/entity/BannerPatternLayers;",
            at = @At("HEAD"))
    private void neatlybetter$captureEmissive(ItemStack stack,
                                              CallbackInfoReturnable<BannerPatternLayers> cir) {
        this.neatlybetter$pendingEmissive =
                Boolean.TRUE.equals(stack.get(ModDataComponents.EMISSIVE_BANNER));
    }

    @Inject(method = "submit*", at = @At("HEAD"))
    private void neatlybetter$setEmissive(BannerPatternLayers layers,
                                          ItemDisplayContext ctx,
                                          PoseStack poseStack,
                                          SubmitNodeCollector collector,
                                          int light, int overlay,
                                          boolean glint, int pass,
                                          CallbackInfo ci) {
        EmissiveBannerState.setEmissive(this.neatlybetter$pendingEmissive);
    }

    @Inject(method = "submit*", at = @At("RETURN"))
    private void neatlybetter$clearEmissive(BannerPatternLayers layers,
                                            ItemDisplayContext ctx,
                                            PoseStack poseStack,
                                            SubmitNodeCollector collector,
                                            int light, int overlay,
                                            boolean glint, int pass,
                                            CallbackInfo ci) {
        EmissiveBannerState.setEmissive(false);
    }
}
