package net.hallowed.oldways.client.mixin.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentRendererMixin {

    @Unique private static final int FULL_BRIGHT = 0xF000F0;

    // 0 = Normal, 1 = Emissive, 2 = Pulsing
    @Unique private static final ThreadLocal<Integer> OW$trimState = ThreadLocal.withInitial(() -> 0);

    @Inject(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V",
            at = @At("HEAD")
    )
    private void ow$markSimple(EquipmentClientInfo.LayerType layerType, ResourceKey<@NotNull EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, PoseStack matrices, SubmitNodeCollector queue, int light, int outlineColor, CallbackInfo ci) {
        int state = 0;
        if (stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) state = 1;
        else if (stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) state = 2;
        OW$trimState.set(state);
    }

    @Inject(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V",
            at = @At("TAIL")
    )
    private void ow$clearSimple(EquipmentClientInfo.LayerType layerType, ResourceKey<@NotNull EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, PoseStack matrices, SubmitNodeCollector queue, int light, int outlineColor, CallbackInfo ci) {
        OW$trimState.remove();
    }

    @Inject(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At("HEAD")
    )
    private void ow$markFull(EquipmentClientInfo.LayerType layerType, ResourceKey<@NotNull EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, PoseStack matrices, SubmitNodeCollector queue, int light, Identifier altTexture, int outlineColor, int order, CallbackInfo ci) {
        int state = 0;
        if (stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) state = 1;
        else if (stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) state = 2;
        OW$trimState.set(state);
    }

    // Safely clear ThreadLocal for the full overload as well
    @Inject(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At("TAIL")
    )
    private void ow$clearFull(EquipmentClientInfo.LayerType layerType, ResourceKey<@NotNull EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, PoseStack matrices, SubmitNodeCollector queue, int light, Identifier altTexture, int outlineColor, int order, CallbackInfo ci) {
        OW$trimState.remove();
    }

    @Redirect(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            )
    )
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void ow$emissiveTrimSubmit(OrderedSubmitNodeCollector queue, Model model, Object stateObj, PoseStack matrices, RenderType layer, int light, int overlay, int tintedColor, TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumble) {

        int useLight = light;
        int state = OW$trimState.get();

        if (sprite != null) {
            if (state == 1) {
                // Static Glow
                useLight = FULL_BRIGHT;
            } else if (state == 2) {
                // Dynamic Pulse (Echo Shard Breathing)
                long time = Util.getMillis();

                // Generates a sine wave that goes from 0.0 to 1.0 smoothly over ~1.25 seconds
                float sine = (Mth.sin(time / 500.0f) + 1.0f) / 2.0f;

                // Map the sine wave to Minecraft's light levels (60 is darkish, 240 is max brightness)
                int currentLight = (int) (60 + sine * 180);

                // Pack block and sky light together into the int
                useLight = (currentLight << 16) | currentLight;
            }
        }

        queue.submitModel(model, stateObj, matrices, layer, useLight, overlay, tintedColor, sprite, outlineColor, crumble);
    }
}