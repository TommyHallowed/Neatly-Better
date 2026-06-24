package net.hallowed.neatlybetter.client.compat;

import dev.kikugie.elytratrims.api.ETClientInitializer;
import dev.kikugie.elytratrims.api.render.ETRenderMethod;
import dev.kikugie.elytratrims.api.render.ETRenderParameters;
import dev.kikugie.elytratrims.api.render.ETRendererID;
import dev.kikugie.elytratrims.api.render.ETRenderingAPI;

import net.hallowed.neatlybetter.init.ModData;


import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

public class ElytraTrimsCompat implements ETClientInitializer {

    @Override
    public void onInitializeClientET() {
        ETRendererID trimType = new ETRendererID(
                Identifier.fromNamespaceAndPath("elytratrims", "trims"),
                ETRenderMethod.POST
        );

        ETRenderingAPI.wrapRenderParameters(trimType, parameters -> {
            ItemStack stack = parameters.stack();

            boolean emissive = stack.getOrDefault(ModData.EMISSIVE_TRIM, false);
            boolean pulsing  = stack.getOrDefault(ModData.PULSING_TRIM, false);

            if (!emissive && !pulsing) return parameters;
            if (parameters.sprite() == null)  return parameters;

            int light = LightCoordsUtil.FULL_BRIGHT;
            int color = parameters.color();

            if (pulsing) {
                color = getPulseColor(color);
            }

            return new ETRenderParameters(
                    parameters.elytra(),
                    parameters.object(),
                    parameters.stack(),
                    parameters.matrices(),
                    parameters.render(),
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
        long period = 4000L;
        float phase = (Util.getMillis() % period) / (float) period * Mth.TWO_PI;
        float sine = (Mth.sin(phase) + 1f) * 0.5f;
        float multiplier = 0.4f + (sine * 0.6f);
        int r = (int) (ARGB.red(color)   * multiplier);
        int g = (int) (ARGB.green(color) * multiplier);
        int b = (int) (ARGB.blue(color)  * multiplier);
        return ARGB.color(ARGB.alpha(color), r, g, b);
    }
}
