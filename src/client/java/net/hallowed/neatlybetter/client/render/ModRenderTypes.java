package net.hallowed.neatlybetter.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class ModRenderTypes {
    private static final Map<Identifier, RenderType> EMISSIVE_BANNER_CACHE = new Object2ObjectOpenHashMap<>();

    public static RenderType getEmissiveBanner(Identifier texture) {
        return EMISSIVE_BANNER_CACHE.computeIfAbsent(texture, tex ->
                RenderType.create(
                        "neatlybetter_emissive_banner_" + tex.getPath(),
                        RenderSetup.builder(RenderPipelines.EYES)
                                .withTexture("Sampler0", tex)
                                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                                .createRenderSetup()
                )
        );
    }
}
