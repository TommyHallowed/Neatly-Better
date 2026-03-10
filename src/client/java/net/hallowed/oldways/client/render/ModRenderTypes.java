package net.hallowed.oldways.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

@Environment(EnvType.CLIENT)
public class ModRenderTypes {

    /**
     * Specialized RenderType for Armor Trims:
     * - Uses RenderPipelines.EYES so Iris triggers Bloom.
     * - Uses VIEW_OFFSET_Z_LAYERING so it renders slightly in front of the armor (fixing invisibility).
     */
    public static final RenderType EMISSIVE_ARMOR_TRIM = RenderType.create(
            "oldways_emissive_armor_trim",
            RenderSetup.builder(RenderPipelines.EYES)
                    .withTexture("Sampler0", Sheets.ARMOR_TRIMS_SHEET)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );
}