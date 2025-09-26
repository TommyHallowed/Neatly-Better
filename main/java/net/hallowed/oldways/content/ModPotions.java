package net.hallowed.oldways.content;

import net.hallowed.TheOldWays; // use your mod class if you keep MOD_ID there
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModPotions {
    public static RegistryEntry<Potion> HASTE;
    public static RegistryEntry<Potion> LONG_HASTE;
    public static RegistryEntry<Potion> STRONG_HASTE;

    private static RegistryEntry<Potion> register(String name, Potion potion) {
        // mirror vanilla Potions: return a RegistryEntry<Potion>
        return Registry.registerReference(
                Registries.POTION,
                Identifier.of(TheOldWays.MOD_ID, name),
                potion
        );
    }

    public static void registerAll() {
        // 3:00 Haste I
        HASTE = register("haste",
                new Potion("haste", new StatusEffectInstance(StatusEffects.HASTE, 20 * 180, 0)));

        // 8:00 Haste I
        LONG_HASTE = register("long_haste",
                new Potion("haste", new StatusEffectInstance(StatusEffects.HASTE, 20 * 480, 0)));

        // 1:30 Haste II
        STRONG_HASTE = register("strong_haste",
                new Potion("haste", new StatusEffectInstance(StatusEffects.HASTE, 20 * 90, 1)));
    }

    private ModPotions() {}
}
