package net.hallowed.oldways.client.mixin.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.hallowed.oldways.client.render.ModRenderTypes;
import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentRendererMixin {

    @Unique
    private static int oldways$getPulseColor(int color) {

        float sine = (Mth.sin(Util.getMillis() / 500f) + 1f) * 0.5f;
        float multiplier = 0.4f + (sine * 0.6f);

        int r = (int)(ARGB.red(color) * multiplier);
        int g = (int)(ARGB.green(color) * multiplier);
        int b = (int)(ARGB.blue(color) * multiplier);

        return ARGB.color(ARGB.alpha(color), r, g, b);
    }

    @Redirect(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            )
    )
    @SuppressWarnings({"rawtypes","unchecked"})
    private void oldways$applyTrimEffects(
            OrderedSubmitNodeCollector instance,
            Model model,
            Object stateObj,
            PoseStack matrices,
            RenderType renderType,
            int light,
            int overlay,
            int color,
            TextureAtlasSprite sprite,
            int outline,
            ModelFeatureRenderer.CrumblingOverlay crumble,
            @Local(argsOnly = true) ItemStack stack
    ) {

        RenderType finalType = renderType;
        int finalLight = light;
        int finalColor = color;

        if (sprite != null && sprite.atlasLocation().getPath().contains("trims")) {

            if (stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false) ||
                    stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) {

                finalType = ModRenderTypes.getEmissiveTrim(sprite.atlasLocation());

                finalLight = 0xF000F0;

                if (stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) {
                    finalColor = oldways$getPulseColor(color);
                }
            }
        }

        instance.submitModel(
                model,
                stateObj,
                matrices,
                finalType,
                finalLight,
                overlay,
                finalColor,
                sprite,
                outline,
                crumble
        );
    }
}