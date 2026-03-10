package net.hallowed.oldways.client.render;

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
    // We change this to a method that accepts the texture ID
    private static final Map<Identifier, RenderType> CACHE = new Object2ObjectOpenHashMap<>();

    public static RenderType getEmissiveTrim(Identifier texture) {
        return CACHE.computeIfAbsent(texture, tex ->
                RenderType.create(
                        "oldways_emissive_trim_" + tex.getPath(),
                        RenderSetup.builder(RenderPipelines.EYES)
                                .withTexture("Sampler0", tex)
                                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                                .createRenderSetup()
                )
        );
    }
}