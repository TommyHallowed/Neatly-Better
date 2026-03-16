package net.hallowed.neatlybetter.client.mixin.render.banner;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.hallowed.neatlybetter.client.render.EmissiveBannerState;
import net.hallowed.neatlybetter.init.ModDataComponents;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemDisplayContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ShieldSpecialRenderer.class)
public abstract class ShieldSpecialRendererMixin {

    @Inject(method = "submit*", at = @At("HEAD"))
    private void neatlybetter$setEmissive(DataComponentMap map,
                                          ItemDisplayContext ctx,
                                          PoseStack poseStack,
                                          SubmitNodeCollector collector,
                                          int light, int overlay,
                                          boolean glint, int pass,
                                          CallbackInfo ci) {
        boolean emissive = map != null
                && Boolean.TRUE.equals(map.get(ModDataComponents.EMISSIVE_BANNER));
        EmissiveBannerState.setEmissive(emissive);
    }

    @Inject(method = "submit*", at = @At("RETURN"))
    private void neatlybetter$clearEmissive(DataComponentMap map,
                                            ItemDisplayContext ctx,
                                            PoseStack poseStack,
                                            SubmitNodeCollector collector,
                                            int light, int overlay,
                                            boolean glint, int pass,
                                            CallbackInfo ci) {
        EmissiveBannerState.setEmissive(false);
    }
}
