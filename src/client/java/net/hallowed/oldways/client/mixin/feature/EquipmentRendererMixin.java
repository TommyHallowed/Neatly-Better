package net.hallowed.oldways.client.mixin.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.content.ModDataComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EquipmentRenderer.class)
public abstract class EquipmentRendererMixin {
    @Unique private static final int FULL_BRIGHT = 0xF000F0;

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    private void oldways$emissiveTrim(EquipmentModel.LayerType layerType,
                                      RegistryKey<EquipmentAsset> assetKey,
                                      Model model, ItemStack stack, MatrixStack matrices,
                                      VertexConsumerProvider consumers, int light, CallbackInfo ci) {

        if (!stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false)) return;

        ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
        if (trim == null) return;

        Identifier spriteId = trim.getTextureId(layerType.getTrimsDirectory(), assetKey);
        Sprite sprite = MinecraftClient.getInstance()
                .getBakedModelManager()
                .getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE)
                .getSprite(spriteId);
        if (sprite == null) return; // safety

        boolean decal = trim.pattern().value().decal();
        VertexConsumer vc = sprite.getTextureSpecificVertexConsumer(
                consumers.getBuffer(TexturedRenderLayers.getArmorTrims(decal))
        );

        model.render(matrices, vc, FULL_BRIGHT, OverlayTexture.DEFAULT_UV);
    }
}
