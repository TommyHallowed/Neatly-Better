package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

public final class ModBrewing {
    private ModBrewing() {}

    public static void register() {
        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            // Haste
            builder.addMix(
                    Potions.AWKWARD,
                    Items.DIAMOND,
                    ModPotions.HASTE
            );

            // Long Haste
            builder.addMix(
                    ModPotions.HASTE,
                    Items.REDSTONE,
                    ModPotions.LONG_HASTE
            );

            // Strong Haste
            builder.addMix(
                    ModPotions.HASTE,
                    Items.GOLD_BLOCK,
                    ModPotions.STRONG_HASTE
            );

            // Strong Long Haste
            builder.addMix(
                    ModPotions.STRONG_HASTE,
                    Items.REDSTONE,
                    ModPotions.STRONG_LONG_HASTE
            );

            builder.addMix(
                    ModPotions.LONG_HASTE,
                    Items.GOLD_BLOCK,
                    ModPotions.STRONG_LONG_HASTE
            );
        });
    }
}
