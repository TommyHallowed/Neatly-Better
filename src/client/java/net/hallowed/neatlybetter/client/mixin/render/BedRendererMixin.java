package net.hallowed.neatlybetter.client.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.hallowed.neatlybetter.init.ModBlocks;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BedRenderer.class)
public abstract class BedRendererMixin {

    @Unique
    private static SpriteId neatlybetter$rainbowSpriteId;

    @Redirect(
            method = "submit*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Sheets;getBedSprite(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/client/resources/model/sprite/SpriteId;"
            )
    )
    private SpriteId neatlybetter$swapRainbowBedSprite(
            DyeColor color,
            BedRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        if (state.blockState.is(ModBlocks.RAINBOW_BED)) {
            if (neatlybetter$rainbowSpriteId == null) {
                neatlybetter$rainbowSpriteId = new SpriteId(
                        Sheets.BED_SHEET,
                        Identifier.fromNamespaceAndPath("neatly-better", "entity/bed/rainbow")
                );
            }
            return neatlybetter$rainbowSpriteId;
        }
        return Sheets.getBedSprite(color);
    }
}