package net.hallowed.oldways.client.mixin.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EquipmentRenderer.class)
public abstract class EquipmentRendererMixin {

    @Unique private static final int FULL_BRIGHT = 0xF000F0;

    // 0 = Normal, 1 = Emissive, 2 = Pulsing
    @Unique private static final ThreadLocal<Integer> OW$trimState = ThreadLocal.withInitial(() -> 0);

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;II)V",
            at = @At("HEAD")
    )
    private void ow$markSimple(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int outlineColor, CallbackInfo ci) {
        int state = 0;
        if (stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) state = 1;
        else if (stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) state = 2;
        OW$trimState.set(state);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;II)V",
            at = @At("TAIL")
    )
    private void ow$clearSimple(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int outlineColor, CallbackInfo ci) {
        OW$trimState.remove();
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
            at = @At("HEAD")
    )
    private void ow$markFull(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier altTexture, int outlineColor, int order, CallbackInfo ci) {
        int state = 0;
        if (stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) state = 1;
        else if (stack.getOrDefault(ModDataComponents.PULSING_TRIM, false)) state = 2;
        OW$trimState.set(state);
    }

    // Safely clear ThreadLocal for the full overload as well
    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
            at = @At("TAIL")
    )
    private void ow$clearFull(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model<?> model, Object object, ItemStack stack, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier altTexture, int outlineColor, int order, CallbackInfo ci) {
        OW$trimState.remove();
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/RenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
            )
    )
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void ow$emissiveTrimSubmit(RenderCommandQueue queue, Model model, Object stateObj, MatrixStack matrices, RenderLayer layer, int light, int overlay, int tintedColor, Sprite sprite, int outlineColor, ModelCommandRenderer.CrumblingOverlayCommand crumble) {

        int useLight = light;
        int state = OW$trimState.get();

        if (sprite != null) {
            if (state == 1) {
                // Static Glow
                useLight = FULL_BRIGHT;
            } else if (state == 2) {
                // Dynamic Pulse (Echo Shard Breathing)
                long time = Util.getMeasuringTimeMs();

                // Generates a sine wave that goes from 0.0 to 1.0 smoothly over ~1.25 seconds
                float sine = (MathHelper.sin(time / 500.0f) + 1.0f) / 2.0f;

                // Map the sine wave to Minecraft's light levels (60 is darkish, 240 is max brightness)
                int currentLight = (int) (60 + sine * 180);

                // Pack block and sky light together into the int
                useLight = (currentLight << 16) | currentLight;
            }
        }

        queue.submitModel(model, stateObj, matrices, layer, useLight, overlay, tintedColor, sprite, outlineColor, crumble);
    }
}