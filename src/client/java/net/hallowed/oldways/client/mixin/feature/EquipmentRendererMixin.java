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
    @Unique private static final ThreadLocal<Boolean> OW$emissiveTrimNow =
            ThreadLocal.withInitial(() -> false);

    /* Mark/clear on the simple overload … */
    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;" +
                    "Lnet/minecraft/registry/RegistryKey;" +
                    "Lnet/minecraft/client/model/Model;" +
                    "Ljava/lang/Object;" +
                    "Lnet/minecraft/item/ItemStack;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;" +
                    "II)V",
            at = @At("HEAD")
    )
    private void ow$markSimple(EquipmentModel.LayerType layerType,
                               RegistryKey<EquipmentAsset> assetKey,
                               Model<?> model,
                               Object object,
                               ItemStack stack,
                               MatrixStack matrices,
                               OrderedRenderCommandQueue queue,
                               int light,
                               int outlineColor,
                               CallbackInfo ci) {
        OW$emissiveTrimNow.set(stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false));
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;" +
                    "Lnet/minecraft/registry/RegistryKey;" +
                    "Lnet/minecraft/client/model/Model;" +
                    "Ljava/lang/Object;" +
                    "Lnet/minecraft/item/ItemStack;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;" +
                    "II)V",
            at = @At("TAIL")
    )
    private void ow$clearSimple(EquipmentModel.LayerType layerType,
                                RegistryKey<EquipmentAsset> assetKey,
                                Model<?> model,
                                Object object,
                                ItemStack stack,
                                MatrixStack matrices,
                                OrderedRenderCommandQueue queue,
                                int light,
                                int outlineColor,
                                CallbackInfo ci) {
        OW$emissiveTrimNow.remove();
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;" +
                    "Lnet/minecraft/registry/RegistryKey;" +
                    "Lnet/minecraft/client/model/Model;" +
                    "Ljava/lang/Object;" +
                    "Lnet/minecraft/item/ItemStack;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;" +
                    "ILnet/minecraft/util/Identifier;II)V",
            at = @At("HEAD")
    )
    private void ow$markFull(EquipmentModel.LayerType layerType,
                             RegistryKey<EquipmentAsset> assetKey,
                             Model<?> model,
                             Object object,
                             ItemStack stack,
                             MatrixStack matrices,
                             OrderedRenderCommandQueue queue,
                             int light,
                             Identifier altTexture,
                             int outlineColor,
                             int order,
                             CallbackInfo ci) {
        OW$emissiveTrimNow.set(stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false));
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;" +
                    "Lnet/minecraft/registry/RegistryKey;" +
                    "Lnet/minecraft/client/model/Model;" +
                    "Ljava/lang/Object;" +
                    "Lnet/minecraft/item/ItemStack;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;" +
                    "ILnet/minecraft/util/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/RenderCommandQueue;" +
                            "submitModel(Lnet/minecraft/client/model/Model;" +
                            "Ljava/lang/Object;" +
                            "Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/RenderLayer;" +
                            "III" +
                            "Lnet/minecraft/client/texture/Sprite;" +
                            "I" +
                            "Lnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
            )
    )
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void ow$emissiveTrimSubmit(RenderCommandQueue queue,
                                       Model model,
                                       Object state,
                                       MatrixStack matrices,
                                       RenderLayer layer,
                                       int light,
                                       int overlay,
                                       int tintedColor,
                                       Sprite sprite,
                                       int outlineColor,
                                       ModelCommandRenderer.CrumblingOverlayCommand crumble) {

        int useLight = (OW$emissiveTrimNow.get() && sprite != null) ? FULL_BRIGHT : light;
        queue.submitModel(model, state, matrices, layer, useLight, overlay, tintedColor, sprite, outlineColor, crumble);
    }

}
