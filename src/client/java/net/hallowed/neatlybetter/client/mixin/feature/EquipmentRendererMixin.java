package net.hallowed.neatlybetter.client.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.mojang.blaze3d.vertex.PoseStack;

import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentRendererMixin {

    @Unique
    private static int neatlybetter$getPulseColor(int color) {
        long period = 4000L;
        float phase = (Util.getMillis() % period) / (float) period * Mth.TWO_PI;
        float sine = (Mth.sin(phase) + 1f) * 0.5f;
        float multiplier = 0.4f + (sine * 0.6f);
        int r = (int) (ARGB.red(color) * multiplier);
        int g = (int) (ARGB.green(color) * multiplier);
        int b = (int) (ARGB.blue(color) * multiplier);
        return ARGB.color(ARGB.alpha(color), r, g, b);
    }

    @SuppressWarnings({"rawtypes"})
    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            )
    )
    private void neatlybetter$applyTrimEffects(
            OrderedSubmitNodeCollector instance,
            Model model,
            Object stateObj,
            PoseStack matrices,
            RenderType renderType,
            int light,
            int overlay,
            int finalColor,
            TextureAtlasSprite sprite,
            int outline,
            ModelFeatureRenderer.CrumblingOverlay crumble,
            Operation<Void> original,
            @Local(argsOnly = true, name = "itemStack") ItemStack itemStack
    ) {
        int finalLight = light;

        if (sprite != null && sprite.atlasLocation().getPath().contains("trims")) {
            boolean emissive = itemStack.getOrDefault(ModData.EMISSIVE_TRIM, false);
            boolean pulsing = itemStack.getOrDefault(ModData.PULSING_TRIM, false);

            if (pulsing) {
                finalLight = LightCoordsUtil.FULL_BRIGHT;
                finalColor = neatlybetter$getPulseColor(finalColor);
            } else if (emissive) {
                finalLight = LightCoordsUtil.FULL_BRIGHT;
            }
        }

        original.call(instance, model, stateObj, matrices, renderType,
                finalLight, overlay, finalColor, sprite, outline, crumble);
    }
}