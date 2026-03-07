package net.hallowed.oldways.init;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

public final class ModBrewing {
    private ModBrewing() {}

    public static void register() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            // Awkward + Diamond -> Haste
            builder.addMix(
                    Potions.AWKWARD,
                    Items.DIAMOND,
                    ModPotions.HASTE
            );

            // Haste + Redstone -> Long Haste
            builder.addMix(
                    ModPotions.HASTE,
                    Items.REDSTONE,
                    ModPotions.LONG_HASTE
            );

            // Haste + Dragon Breath -> Strong Haste
            builder.addMix(
                    ModPotions.HASTE,
                    Items.DRAGON_BREATH,
                    ModPotions.STRONG_HASTE
            );

            // Haste II + Redstone -> Strong Long Haste
            builder.addMix(
                    ModPotions.STRONG_HASTE,
                    Items.REDSTONE,
                    ModPotions.STRONG_LONG_HASTE
            );

            // Long Haste + Dragon Breath -> Strong Long Haste
            builder.addMix(
                    ModPotions.LONG_HASTE,
                    Items.DRAGON_BREATH,
                    ModPotions.STRONG_LONG_HASTE
            );
        });
    }
}
