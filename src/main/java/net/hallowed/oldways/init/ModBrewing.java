package net.hallowed.oldways.init;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

public final class ModBrewing {
    private ModBrewing() {}

    public static void register() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            // Awkward + Diamond -> Haste
            builder.registerPotionRecipe(
                    Potions.AWKWARD,
                    Items.DIAMOND,
                    ModPotions.HASTE
            );

            // Haste + Redstone -> Long Haste
            builder.registerPotionRecipe(
                    ModPotions.HASTE,
                    Items.REDSTONE,
                    ModPotions.LONG_HASTE
            );

            // Haste + Dragon Breath -> Strong Haste
            builder.registerPotionRecipe(
                    ModPotions.HASTE,
                    Items.DRAGON_BREATH,
                    ModPotions.STRONG_HASTE
            );

            // Haste II + Redstone -> Strong Long Haste
            builder.registerPotionRecipe(
                    ModPotions.STRONG_HASTE,
                    Items.REDSTONE,
                    ModPotions.STRONG_LONG_HASTE
            );

            // Long Haste + Dragon Breath -> Strong Long Haste
            builder.registerPotionRecipe(
                    ModPotions.LONG_HASTE,
                    Items.DRAGON_BREATH,
                    ModPotions.STRONG_LONG_HASTE
            );
        });
    }
}
