package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

public final class ModBrewing {
    private ModBrewing() {}

    public static void register() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
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
                    Items.GOLDEN_CARROT,
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
                    Items.GOLDEN_CARROT,
                    ModPotions.STRONG_LONG_HASTE
            );
        });
    }
}
