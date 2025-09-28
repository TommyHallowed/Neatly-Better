package net.hallowed.oldways.content;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

/** Registers brewing recipes for custom potions. */
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

            // Haste + Glowstone -> Strong Haste
            builder.registerPotionRecipe(
                    ModPotions.HASTE,
                    Items.GLOWSTONE_DUST,
                    ModPotions.STRONG_HASTE
            );
        });
    }
}
