package net.hallowed.oldways.client.mixin.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentRendererMixin {

    @Redirect(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    ordinal = 2
            )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void oldways$applyEmissiveTrim(
            OrderedSubmitNodeCollector instance, Model model, Object stateObj, PoseStack matrices,
            RenderType originalType, int light, int overlay, int tintedColor,
            TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumble,
            @Local(argsOnly = true) ItemStack stack
    ) {
        RenderType finalType = originalType;
        int finalLight = light;
        int finalColor = tintedColor;

        if (stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false) ||
                stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) {

            finalType = ModRenderTypes.EMISSIVE_ARMOR_TRIM;

            if (stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) {
                float sine = (Mth.sin(Util.getMillis() / 500.0f) + 1.0f) / 2.0f;
                int brightness = (int) (100 + (sine * 155));
                finalColor = ARGB.color(255, brightness, brightness, brightness);
            } else {
                finalLight = 0xF000F0; // Full bright
            }
        }

        instance.submitModel(model, stateObj, matrices, finalType, finalLight, overlay, finalColor, sprite, outlineColor, crumble);
    }
}