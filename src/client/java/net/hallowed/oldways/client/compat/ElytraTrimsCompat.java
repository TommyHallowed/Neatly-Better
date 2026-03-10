package net.hallowed.oldways.client.compat;

import dev.kikugie.elytratrims.api.ETClientInitializer;
import dev.kikugie.elytratrims.api.render.ETRenderMethod;
import dev.kikugie.elytratrims.api.render.ETRenderParameters;
import dev.kikugie.elytratrims.api.render.ETRendererID;
import dev.kikugie.elytratrims.api.render.ETRenderingAPI;
import net.hallowed.oldways.client.render.ModRenderTypes;
import net.hallowed.oldways.init.ModDataComponents;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

/**
 * Integrates Old Ways' emissive/pulsing trim effects with ElytraTrim's
 * decorator rendering pipeline.

 * Registered as an "elytratrims-client" entrypoint — only loaded when
 * ElytraTrim is present (safe as an optional dependency).
 */
public class ElytraTrimsCompat implements ETClientInitializer {

    @Override
    public void onInitializeClientET() {
        ETRendererID trimType = new ETRendererID(
                Identifier.fromNamespaceAndPath("elytratrims", "trims"),
                ETRenderMethod.POST
        );

        ETRenderingAPI.wrapRenderParameters(trimType, parameters -> {
            ItemStack stack = parameters.stack();

            boolean emissive = stack.getOrDefault(ModDataComponents.EMISSIVE_TRIM, false);
            boolean pulsing  = stack.getOrDefault(ModDataComponents.PULSING_TRIM, false);

            if (!emissive && !pulsing) return parameters;
            if (parameters.sprite() == null)  return parameters;

            RenderType emissiveType = ModRenderTypes.getEmissiveTrim(
                    parameters.sprite().atlasLocation()
            );

            int light = 0xF000F0;
            int color = parameters.color();

            if (pulsing) {
                color = getPulseColor(color);
            }

            return new ETRenderParameters(
                    parameters.elytra(),
                    parameters.object(),
                    parameters.stack(),
                    parameters.matrices(),
                    emissiveType,
                    parameters.sprite(),
                    parameters.texture(),
                    light,
                    color,
                    parameters.overlay(),
                    parameters.outline(),
                    parameters.order()
            );
        });
    }

    private static int getPulseColor(int color) {
        float sine = (Mth.sin(Util.getMillis() / 500f) + 1f) * 0.5f;
        float multiplier = 0.4f + (sine * 0.6f);

        int r = (int) (ARGB.red(color)   * multiplier);
        int g = (int) (ARGB.green(color) * multiplier);
        int b = (int) (ARGB.blue(color)  * multiplier);

        return ARGB.color(ARGB.alpha(color), r, g, b);
    }
}