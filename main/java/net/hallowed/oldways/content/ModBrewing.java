package net.hallowed.oldways.content;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

public final class ModBrewing {
    public static void register() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            // Awkward + Amethyst Shard -> Haste
            builder.registerPotionRecipe(
                    Potions.AWKWARD,            // already RegistryEntry<Potion>
                    Items.DIAMOND,
                    ModPotions.HASTE);          // RegistryEntry<Potion>

            // Haste + Redstone -> Long Haste
            builder.registerPotionRecipe(
                    ModPotions.HASTE,
                    Items.REDSTONE,
                    ModPotions.LONG_HASTE);

            // Haste + Glowstone -> Strong Haste
            builder.registerPotionRecipe(
                    ModPotions.HASTE,
                    Items.GLOWSTONE_DUST,
                    ModPotions.STRONG_HASTE);
        });
    }

    private ModBrewing() {}
}
