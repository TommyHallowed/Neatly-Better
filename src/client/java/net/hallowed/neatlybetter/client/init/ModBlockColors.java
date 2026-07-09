package net.hallowed.neatlybetter.client.init;

import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;

import net.hallowed.neatlybetter.init.ModBlocks;

import net.minecraft.util.ARGB;

public final class ModBlockColors {
    private ModBlockColors() {}

    private static final int NO_TINT = -1;

    public static void register() {
        BlockColorRegistry.register((state, level, pos, tintValues) -> {
            tintValues.size(1);

            int tint = NO_TINT;
            Object renderData = level.getBlockEntityRenderData(pos);
            if (renderData instanceof Integer color && color != -1) {
                tint = ARGB.opaque(color);
            }

            tintValues.set(0, tint);
        }, ModBlocks.DYE_CAULDRON);
    }
}
